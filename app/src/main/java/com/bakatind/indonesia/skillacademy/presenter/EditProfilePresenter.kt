package com.bakatind.indonesia.skillacademy.presenter

import android.content.Context
import com.bakatind.indonesia.skillacademy.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bakatind.indonesia.skillacademy.model.StudentResponse
import com.bakatind.indonesia.skillacademy.util.Validation.Companion.validateEmail
import com.bakatind.indonesia.skillacademy.util.Validation.Companion.validateFields
import com.bakatind.indonesia.skillacademy.view.EditProfileView

class EditProfilePresenter(
    private val context: Context,
    private val view: EditProfileView.View
) : EditProfileView.Presenter {

    private val mUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

    private lateinit var mDefaultEmail: String

    override fun requestDataUser() {
        val db = FirebaseFirestore.getInstance()
            .collection("users")
            .document(mUserId)

        db.get()
            .addOnSuccessListener {
                val name = it.getString("username").toString()
                mDefaultEmail = it.getString("email").toString()
                val school = it.getString("school").toString()
                val address = it.getString("address").toString()
                val phone = it.getString("phone").toString()
                val datetime = it.getString("datetime").toString()

                view.hideProgressBar()
                view.showProfileUser(name, mDefaultEmail, school, address, phone, datetime)

            }.addOnFailureListener {
                view.hideProgressBar()
                view.handleResponse(it.localizedMessage?.toString().toString())
            }
    }

    override fun requestEditProfile(
        name: String,
        email: String,
        school: String,
        address: String,
        phone: String,
        datetime: String
    ) {
        val student = StudentResponse()
        student.username = name
        student.email = email
        student.school = school
        student.address = address
        student.phone = phone
        student.teacher = false
        student.active = true
        student.datetime = datetime

        if (validateFields(name)
            || validateFields(school) || validateFields(address) || validateFields(phone)
        ) {
            view.handleResponse(context.getString(R.string.warning_input_data))
        } else if (validateEmail(email)) {
            view.handleResponse(context.getString(R.string.email_not_valid))
        } else {
            if (mDefaultEmail == email) {
                editDataUser(student)
            } else {
                val mAuth = FirebaseAuth.getInstance().currentUser
                mAuth?.updateEmail(email)
                    ?.addOnCompleteListener {
                        editDataUser(student)
                    }
                    ?.addOnFailureListener {
                        view.handleResponse(it.localizedMessage?.toString().toString())
                    }
            }
        }
    }

    private fun editDataUser(student: StudentResponse) {
        view.showProgressBar()

        val db = FirebaseFirestore.getInstance()
            .collection("users").document(mUserId)

        db.set(student)
            .addOnSuccessListener {
                view.onSuccessSaveData(context.getString(R.string.success_edit_profile))
            }.addOnFailureListener {
                view.hideProgressBar()
                view.handleResponse(it.localizedMessage?.toString().toString())
            }
    }
}