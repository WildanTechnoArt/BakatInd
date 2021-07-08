package com.bakatind.indonesia.skillacademy.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.bakatind.indonesia.skillacademy.GlideApp
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.activity.teacher.TeacherMainActivity
import com.bakatind.indonesia.skillacademy.database.SharedPrefManager
import com.bakatind.indonesia.skillacademy.presenter.LoginPresenter
import com.bakatind.indonesia.skillacademy.view.LoginView
import com.firebase.ui.auth.AuthUI
import com.google.firebase.firestore.FieldValue
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.progressHorizontal
import java.net.URLEncoder

class LoginActivity : AppCompatActivity(), LoginView.View {

    private lateinit var presenter: LoginView.Presenter

    private lateinit var mEmail: String
    private lateinit var mPassword: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        prepare()

        btn_login.setOnClickListener {
            mEmail = input_email.text.toString().trim()
            mPassword = input_password.text.toString().trim()
            presenter.requestLogin(mEmail, mPassword)
        }

        tv_forgot_password.setOnClickListener {
            startActivity(Intent(this, ForgotPassActivity::class.java))
        }

        tv_register.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)
                .setTitle("Pilih Jenis Pengguna")
                .setMessage("Ingin Mendaftar Sebagai?")
                .setPositiveButton("Pemateri") { _, _ ->
                    startActivity(Intent(this, RegisterTeacherActivity::class.java))
                }
                .setNegativeButton("Pelajar") { _, _ ->
                    startActivity(Intent(this, RegisterStudentActivity::class.java))
                }
            val dialog = builder.create()
            dialog.show()
        }
    }

    override fun onSuccess(result: DocumentSnapshot) {
        val getEmail = result.getString("email").toString()
        var deviceLoginCount: Long = 1
        val isActive = result.getBoolean("active")
        val deviceLogin = result.getLong("device")
        val getUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        if ((deviceLogin ?: 0) < 0) {
            deviceLoginCount = 2
        }

        if ((deviceLogin ?: 0) > 3) {
            this.let { it1 ->
                AuthUI.getInstance()
                    .signOut(it1)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            showAlertDialog(getString(R.string.is_login))
                        } else {
                            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT)
                                .show()
                        }
                        btn_login.hideProgress(R.string.btn_login)
                    }
            }
        } else {
            if ((isActive == true) && result.exists()) {
                val db = FirebaseFirestore.getInstance()
                db.collection("users")
                    .document(getUserId)
                    .update("device", FieldValue.increment(deviceLoginCount))
                    .addOnSuccessListener {
                        SharedPrefManager.getInstance(this)?.setEmailUser(getEmail)
                        if (result.getBoolean("teacher") == true) {
                            SharedPrefManager.getInstance(this)?.isTeacher(true)
                            startActivity(Intent(this, TeacherMainActivity::class.java))
                            finishAffinity()
                        } else {
                            SharedPrefManager.getInstance(this)?.isTeacher(false)
                            startActivity(Intent(this, MainActivity::class.java))
                            finishAffinity()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, it.localizedMessage?.toString(), Toast.LENGTH_SHORT)
                            .show()
                    }
            } else {
                showAlertDialog(getString(R.string.policy_login))
            }
        }
    }

    override fun handleResponse(message: String) {
        when (message) {
            "ERROR_USER_NOT_FOUND" -> Toast.makeText(
                this, getString(R.string.error_user_not_found),
                Toast.LENGTH_SHORT
            ).show()

            "ERROR_WRONG_PASSWORD" -> Toast.makeText(
                this, getString(R.string.error_wrong_password),
                Toast.LENGTH_SHORT
            ).show()

            else -> Toast.makeText(
                this, message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun showProgressBar() {
        btn_login.showProgress { progressColor = Color.WHITE }
    }

    override fun hideProgressBar() {
        btn_login.hideProgress(R.string.btn_login)
    }

    private fun prepare() {
        presenter = LoginPresenter(this, this)
        GlideApp.with(applicationContext)
            .load(R.drawable.logo_bakain_transparan)
            .into(img_logo)

        tv_forgot_password.paintFlags = tv_forgot_password.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        tv_register.paintFlags = tv_register.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        bindProgressButton(btn_login)
        btn_login.attachTextChangeAnimator()
    }

    private fun showAlertDialog(message: String) {
        val builder = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)
            .setMessage(message)
            .setPositiveButton("Hubungi Admin") { _, _ ->
                chatAdmin()
            }
            .setNegativeButton("Tutup") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun chatAdmin() {
        progressHorizontal.visibility = View.VISIBLE

        val db = FirebaseFirestore.getInstance()
        db.collection("admin")
            .document("nomor WA")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    progressHorizontal.visibility = View.GONE
                    Toast.makeText(
                        this,
                        error.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    progressHorizontal.visibility = View.GONE

                    val getAdmin = value?.getString("admin")

                    val phoneNumber = getAdmin?.replace("+62", "+62 ")
                    val message = ""

                    val url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" +
                            URLEncoder.encode(message, "UTF-8")

                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    startActivity(i)
                }
            }
    }
}