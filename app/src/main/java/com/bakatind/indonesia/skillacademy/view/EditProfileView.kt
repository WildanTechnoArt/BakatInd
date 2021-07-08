package com.bakatind.indonesia.skillacademy.view

class EditProfileView {

    interface View {
        fun onSuccessSaveData(message: String)
        fun showProfileUser(name: String, email: String, school: String, address: String, phone: String, datetime: String)
        fun handleResponse(message: String)
        fun hideProgressBar()
        fun showProgressBar()
    }

    interface Presenter {
        fun requestDataUser()
        fun requestEditProfile(name: String, email: String, school: String, address: String, phone: String, datetime: String)
    }
}