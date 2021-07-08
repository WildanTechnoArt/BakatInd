package com.bakatind.indonesia.skillacademy.presenter

import android.content.Context
import com.bakatind.indonesia.skillacademy.R
import com.google.firebase.auth.FirebaseAuth
import com.bakatind.indonesia.skillacademy.util.Validation.Companion.validateEmail
import com.bakatind.indonesia.skillacademy.view.ForgotPasswordView

class ForgotPasswordPresenter(private val context: Context,
                              private val view: ForgotPasswordView.View) : ForgotPasswordView.Presenter {

    override fun requestForgotPassword(email: String) {
        if (validateEmail(email)) {
            view.handleResponse(context.getString(R.string.email_not_valid))
        } else {
            view.showProgressBar()

            val mAuth = FirebaseAuth.getInstance()

            mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener {

                    if (it.isSuccessful) {
                        view.onSuccess(context.getString(R.string.send_reset_password))
                        view.hideProgressBar()
                    } else {
                        view.hideProgressBar()
                        view.handleResponse(context.getString(R.string.email_not_valid))
                    }
                }
        }
    }
}