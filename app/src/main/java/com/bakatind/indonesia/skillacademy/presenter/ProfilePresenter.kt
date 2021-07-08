package com.bakatind.indonesia.skillacademy.presenter

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.bakatind.indonesia.skillacademy.R
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.bakatind.indonesia.skillacademy.view.ProfileFragmentView
import com.google.firebase.firestore.FieldValue
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import java.io.ByteArrayOutputStream
import java.io.File

class ProfilePresenter(
    private val context: Context?,
    private val view: ProfileFragmentView.View
) : ProfileFragmentView.Presenter {

    private val photoReference = FirebaseStorage.getInstance().reference
    private val thumbReference = FirebaseStorage.getInstance().reference
    private val database = FirebaseFirestore.getInstance()
    private lateinit var listener: ListenerRegistration
    private val mAuth = FirebaseAuth.getInstance().currentUser

    override fun requestDataUser() {
        view.showProgressBar()

        val mUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        val reference = database.collection("users").document(mUserId)

        listener = reference.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val name = snapshot.getString("username").toString()
                val email = snapshot.getString("email").toString()
                val school = snapshot.getString("school").toString()
                val address = snapshot.getString("address").toString()
                val phone = snapshot.getString("phone").toString()
                val datetime = snapshot.getString("datetime").toString()

                view.hideProgressBar()
                view.showProfileUser(name, email, school, address, phone, datetime)
            }
        }
    }

    override fun getPhotoFromStorage() {
        view.showProgressBar()

        database.collection("photos").document(mAuth?.uid.toString())
            .get()
            .addOnSuccessListener {
                val photoUrl: String = it.getString("photoUrl").toString()

                view.showPhotoProfile(photoUrl)
                view.hideProgressBar()

            }.addOnFailureListener {
                view.hideProgressBar()
                view.handleResponse(it.localizedMessage?.toString().toString())
            }
    }

    override fun uploadPhotoProfile(result: CropImage.ActivityResult) {
        view.showProgressBar()

        val resultUri = result.uri

        val thumbImage = File(resultUri.path.toString())

        val thumbBitmap = Compressor(context)
            .setMaxHeight(200)
            .setMaxWidth(200)
            .setQuality(75)
            .compressToBitmap(thumbImage)

        val baos = ByteArrayOutputStream()
        thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageByte = baos.toByteArray()

        val imageURL = "photo_profile/${mAuth?.uid}.jpg"
        val thumbURL = "thumb_image/${mAuth?.uid}.jpg"
        val imagePath = photoReference.child(imageURL)
        val thumbPath = thumbReference.child(thumbURL)

        imagePath.putFile(resultUri).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                photoReference.child(imageURL).downloadUrl.addOnSuccessListener { imageUri: Uri? ->

                    val uploadTask: UploadTask = thumbPath.putBytes(imageByte)
                    val downloadUrl = imageUri.toString()

                    uploadTask.addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            thumbReference.child(thumbURL).downloadUrl.addOnSuccessListener { thumbUri: Uri? ->

                                val thumbUrl = thumbUri.toString()

                                val dataMap = HashMap<String, String>()
                                dataMap["photoUrl"] = downloadUrl
                                dataMap["thumbUrl"] = thumbUrl

                                database.collection("photos").document(mAuth?.uid.toString())
                                    .set(dataMap)
                                    .addOnCompleteListener {
                                        if (task.isSuccessful) {
                                            view.onSuccessUpload(
                                                context,
                                                context?.getString(R.string.upload_success)
                                            )
                                        }
                                    }
                            }

                        } else {
                            view.hideProgressBar()
                            view.handleResponse(task.exception?.localizedMessage.toString())
                        }
                    }

                }.addOnFailureListener {
                    view.hideProgressBar()
                    view.handleResponse(it.localizedMessage?.toString().toString())
                }

            } else {
                view.hideProgressBar()
                view.handleResponse(task.exception?.localizedMessage.toString())
            }
        }
    }

    override fun requestLogout() {
        view.showProgressBar()

        val getUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        database.collection("users")
            .document(getUserId)
            .update("device", FieldValue.increment(-1))
            .addOnSuccessListener {
                context?.let { it1 ->
                    AuthUI.getInstance()
                        .signOut(it1)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                view.onSuccessLogout()
                            } else {
                                view.hideProgressBar()
                                view.handleResponse(it.exception?.localizedMessage.toString())
                            }
                        }
                }
            }
            .addOnFailureListener {
                view.hideProgressBar()
                view.handleResponse(it.localizedMessage?.toString())
            }
    }

    override fun onDestroy() {
        listener.remove()
    }
}