package com.bakatind.indonesia.skillacademy.presenter

import android.content.Context
import com.bakatind.indonesia.skillacademy.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.bakatind.indonesia.skillacademy.util.Validation.Companion.validateEmail
import com.bakatind.indonesia.skillacademy.util.Validation.Companion.validateFields
import com.bakatind.indonesia.skillacademy.view.LoginView

class LoginPresenter(private val context: Context,
                     private val view: LoginView.View
) : LoginView.Presenter {

    override fun requestLogin(email: String, password: String) {
        if (validateFields(email) || validateFields(password)) {
            view.handleResponse(context.getString(R.string.email_password_null))
        } else if (validateEmail(email)) {
            view.handleResponse(context.getString(R.string.email_not_valid))
        } else {
            view.showProgressBar()
            login(email, password)
        }
    }

    private fun login(email: String, password: String) {
        val mAuth = FirebaseAuth.getInstance()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val userId = task.result?.user?.uid.toString()

                    val db = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .get()

                    db.addOnSuccessListener { result ->
                        view.onSuccess(result)
                    }.addOnFailureListener {
                        view.hideProgressBar()
                        view.handleResponse(it.localizedMessage?.toString().toString())
                    }

                } else {
                    view.hideProgressBar()
                    view.handleResponse((task.exception as FirebaseAuthException).errorCode)
                }
            }
    }
}