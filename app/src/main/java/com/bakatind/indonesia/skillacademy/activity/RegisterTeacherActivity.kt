package com.bakatind.indonesia.skillacademy.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.activity.teacher.TeacherMainActivity
import com.bakatind.indonesia.skillacademy.database.SharedPrefManager
import com.bakatind.indonesia.skillacademy.model.TeacherResponse
import com.bakatind.indonesia.skillacademy.util.Validation
import kotlinx.android.synthetic.main.activity_register_teacher.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.text.SimpleDateFormat
import java.util.*

class RegisterTeacherActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mUsername: String
    private lateinit var mEmail: String
    private lateinit var mExperience: String
    private lateinit var mAddress: String
    private lateinit var mPhoneNumber: String
    private lateinit var mPassword: String
    private lateinit var mReTypePassword: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_teacher)
        prepare()
        btn_register.setOnClickListener {
            createAccount()
        }
    }

    private fun prepare() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Daftar Sebagai Pemateri"
        }

        bindProgressButton(btn_register)
        btn_register.attachTextChangeAnimator()

        mAuth = FirebaseAuth.getInstance()
    }

    private fun createAccount() {
        mUsername = input_name.text.toString()
        mEmail = input_email.text.toString()
        mExperience = input_experience.text.toString()
        mAddress = input_address.text.toString()
        mPhoneNumber = input_phone.text.toString()
        mPassword = input_password.text.toString().trim()
        mReTypePassword = input_retype_password.text.toString().trim()

        if (Validation.validateFields(mUsername) || Validation.validateFields(mExperience) ||
            Validation.validateFields(mPassword) || Validation.validateFields(mAddress)
        ) {

            Toast.makeText(
                this, getString(R.string.warning_input_data),
                Toast.LENGTH_SHORT
            ).show()

        } else if (Validation.validateEmail(mEmail)) {

            Toast.makeText(
                this, getString(R.string.email_not_valid),
                Toast.LENGTH_SHORT
            ).show()

        } else {
            if (mPassword == mReTypePassword) {
                btn_register.showProgress { progressColor = Color.WHITE }

                mAuth.createUserWithEmailAndPassword(mEmail, mPassword)
                    .addOnSuccessListener {
                        it.user?.uid?.let { it1 -> addDataUser(it1) }
                    }.addOnFailureListener {
                        btn_register.hideProgress(R.string.btn_register)
                        Toast.makeText(
                            this, it.localizedMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(
                    this, getString(R.string.password_not_valid),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun addDataUser(userId: String) {
        val formatter = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val date: String = formatter.format(Calendar.getInstance().time)

        val teacher = TeacherResponse()
        teacher.username = mUsername
        teacher.email = mEmail
        teacher.experience = mExperience
        teacher.address = mAddress
        teacher.phone = mPhoneNumber
        teacher.datetime = date
        teacher.teacher = true
        teacher.approve = false
        teacher.device = 1
        teacher.active = true

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .set(teacher)
            .addOnSuccessListener {
                val db2 = FirebaseFirestore.getInstance()
                db2.collection("klikkerja")
                    .document("teacher")
                    .collection("newRegistrants")
                    .document(userId)
                    .set(teacher)
                    .addOnSuccessListener {
                        SharedPrefManager.getInstance(this)?.setEmailUser(mEmail)
                        SharedPrefManager.getInstance(this)?.isTeacher(true)
                        startActivity(Intent(this, TeacherMainActivity::class.java))
                        finishAffinity()
                    }.addOnFailureListener {
                        btn_register.hideProgress(R.string.btn_register)

                        Toast.makeText(
                            this, it.localizedMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }.addOnFailureListener {
                btn_register.hideProgress(R.string.btn_register)

                Toast.makeText(
                    this, it.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}