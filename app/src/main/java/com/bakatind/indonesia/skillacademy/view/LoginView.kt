package com.bakatind.indonesia.skillacademy.view

import com.google.firebase.firestore.DocumentSnapshot

class LoginView {

    interface View {
        fun onSuccess(result: DocumentSnapshot)
        fun handleResponse(message: String)
        fun showProgressBar()
        fun hideProgressBar()
    }

    interface Presenter {
        fun requestLogin(email: String, password: String)
    }
}