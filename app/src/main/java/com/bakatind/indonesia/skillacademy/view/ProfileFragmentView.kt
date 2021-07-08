package com.bakatind.indonesia.skillacademy.view

import android.content.Context
import com.theartofdev.edmodo.cropper.CropImage

class ProfileFragmentView {

    interface View {
        fun onSuccessLogout()
        fun onSuccessUpload(context: Context?, message: String?)
        fun showProfileUser(name: String, email: String, school: String, address: String, phone: String, datetime: String)
        fun showPhotoProfile(photoUrl: String)
        fun handleResponse(message: String?)
        fun hideProgressBar()
        fun showProgressBar()
    }

    interface Presenter {
        fun requestDataUser()
        fun getPhotoFromStorage()
        fun uploadPhotoProfile(result: CropImage.ActivityResult)
        fun requestLogout()
        fun onDestroy()
    }
}