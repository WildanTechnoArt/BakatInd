package com.bakatind.indonesia.skillacademy.activity

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bakatind.indonesia.skillacademy.GlideApp
import com.bakatind.indonesia.skillacademy.R
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.bakatind.indonesia.skillacademy.presenter.ForgotPasswordPresenter
import com.bakatind.indonesia.skillacademy.view.ForgotPasswordView
import kotlinx.android.synthetic.main.activity_forgot_pass.*
import kotlinx.android.synthetic.main.activity_forgot_pass.img_logo
import kotlinx.android.synthetic.main.activity_forgot_pass.input_email
import kotlinx.android.synthetic.main.toolbar_layout.*

class ForgotPassActivity : AppCompatActivity(), ForgotPasswordView.View {

    private lateinit var mEmail: String
    private lateinit var presenter: ForgotPasswordView.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass)
        prepare()
        btn_forgot_password.setOnClickListener {
            mEmail = input_email.text.toString()
            presenter.requestForgotPassword(mEmail)
        }
    }

    override fun onSuccess(message: String) {
        Toast.makeText(
            this, message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun handleResponse(message: String) {
        Toast.makeText(
            this, message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun showProgressBar() {
        btn_forgot_password.showProgress { progressColor = Color.WHITE }
    }

    override fun hideProgressBar() {
        btn_forgot_password.hideProgress(R.string.btn_reset_password)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun prepare() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Lupa Password"
        }

        GlideApp.with(applicationContext)
            .load(R.drawable.logo_bakain_transparan)
            .into(img_logo)

        presenter = ForgotPasswordPresenter(this, this)

        bindProgressButton(btn_forgot_password)
        btn_forgot_password.attachTextChangeAnimator()
    }
}