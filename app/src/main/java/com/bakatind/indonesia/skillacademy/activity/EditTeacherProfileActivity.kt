package com.bakatind.indonesia.skillacademy.activity

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.presenter.EditTeacherProfilePresenter
import com.bakatind.indonesia.skillacademy.view.EditProfileView
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import kotlinx.android.synthetic.main.activity_edit_teacher_profile.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class EditTeacherProfileActivity : AppCompatActivity(), EditProfileView.View {

    private lateinit var mNewUsername: String
    private lateinit var mNewEmail: String
    private lateinit var mNewSchool: String
    private lateinit var mNewAddress: String
    private lateinit var mNewPhoneNumber: String
    private lateinit var mDatetime: String

    private lateinit var presenter: EditProfileView.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_teacher_profile)

        prepare()

        presenter.requestDataUser()

        swipe_refresh?.setOnRefreshListener {
            presenter.requestDataUser()
        }

        btn_edit.setOnClickListener {
            mNewUsername = input_name.text.toString()
            mNewEmail = input_email.text.toString()
            mNewSchool = input_experience.text.toString()
            mNewAddress = input_address.text.toString()
            mNewPhoneNumber = input_phone.text.toString()
            presenter.requestEditProfile(
                mNewUsername,
                mNewEmail,
                mNewSchool,
                mNewAddress,
                mNewPhoneNumber,
                mDatetime
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onSuccessSaveData(message: String) {
        Toast.makeText(
            this, message,
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }

    override fun showProfileUser(
        name: String,
        email: String,
        school: String,
        address: String,
        phone: String,
        datetime: String
    ) {
        input_name.setText(name)
        input_email.setText(email)
        input_experience.setText(school)
        input_address.setText(address)
        input_phone.setText(phone)
        mDatetime = datetime
    }

    override fun handleResponse(message: String) {
        btn_edit.hideProgress(R.string.btn_edit)
        Toast.makeText(
            this, message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun hideProgressBar() {
        swipe_refresh?.isRefreshing = false
    }

    override fun showProgressBar() {
        btn_edit.showProgress { progressColor = Color.WHITE }
    }

    private fun prepare() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Edit Profil"
        }

        swipe_refresh?.isRefreshing = true
        presenter = EditTeacherProfilePresenter(this, this)

        bindProgressButton(btn_edit)
        btn_edit.attachTextChangeAnimator()
    }
}