package com.bakatind.indonesia.skillacademy.activity.student

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bakatind.indonesia.skillacademy.GlideApp
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.GET_PROFILE
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_student_profile.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class StudentProfileActivity : AppCompatActivity() {

    private var mPhotoUrl: String? = null
    private var mName: String? = null
    private var mUserId: String? = null
    private var mEmail: String? = null
    private var mPhone: String? = null
    private var mAddress: String? = null
    private var mSchool: String? = null
    private var mDatetime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_profile)
        init()
        getRegistrantStudent()
        showPhotoProfile()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Profil"
        mUserId = intent?.getStringExtra(GET_PROFILE)
            .toString()
        swipe_refresh?.setOnRefreshListener {
            showPhotoProfile()
            getRegistrantStudent()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getRegistrantStudent() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(mUserId.toString())
            .addSnapshotListener { it, error ->
                swipe_refresh?.isRefreshing = false

                if (error != null) {
                    Toast.makeText(this, error.localizedMessage, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                } else {
                    mName = it?.getString("username")
                    mSchool = it?.getString("school")
                    mEmail = it?.getString("email").toString()
                    mPhone = it?.getString("phone").toString()
                    mAddress = it?.getString("address").toString()
                    mDatetime = it?.getString("datetime").toString()

                    tv_name?.text = mName
                    tv_class?.text = mEmail
                    tv_phone_number?.text = mPhone
                    tv_school?.text = mSchool
                    tv_address?.text = mAddress
                    tv_datetime?.text = "Terdaftar Sejak: $mDatetime"
                }
            }
    }
    private fun showPhotoProfile() {
        val db = FirebaseFirestore.getInstance()
        db.collection("photos")
            .document(mUserId.toString())
            .get()
            .addOnSuccessListener {
                swipe_refresh?.isRefreshing = false

                mPhotoUrl = it.getString("photoUrl").toString()

                GlideApp.with(applicationContext)
                    .load(mPhotoUrl)
                    .placeholder(R.drawable.profile_placeholder)
                    .into(img_profile)

            }.addOnFailureListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(this, it.localizedMessage?.toString(), Toast.LENGTH_SHORT).show()
            }
    }
}