package com.bakatind.indonesia.skillacademy.fragment.teacher

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bakatind.indonesia.skillacademy.GlideApp
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.activity.EditTeacherProfileActivity
import com.bakatind.indonesia.skillacademy.activity.LoginActivity
import com.bakatind.indonesia.skillacademy.database.SharedPrefManager
import com.bakatind.indonesia.skillacademy.presenter.TeacherProfilePresenter
import com.bakatind.indonesia.skillacademy.util.UtilsConstant
import com.bakatind.indonesia.skillacademy.view.ProfileFragmentView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.fragment_teacher_profile.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class TeacherProfileFragment : Fragment(), ProfileFragmentView.View {

    private lateinit var presenter: ProfileFragmentView.Presenter
    private lateinit var mContext: Context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_teacher_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        prepare(view)

        presenter.requestDataUser()
        presenter.getPhotoFromStorage()

        swipe_refresh?.setOnRefreshListener {
            presenter.requestDataUser()
        }

        fab_change_photo.setOnClickListener {
            getPhotoFromStorage(it.context)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                val alert = MaterialAlertDialogBuilder(mContext, R.style.MaterialAlertDialog_rounded)
                    .setTitle("Konfirmasi")
                    .setMessage("Anda yakin ingin keluar?")
                    .setPositiveButton("Ya") { _, _ ->
                        presenter.requestLogout()
                    }
                    .setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss()
                    }
                alert.create()
                alert.show()
            }
            R.id.edit_profile -> {
                startActivity(Intent(mContext, EditTeacherProfileActivity::class.java))
            }
        }
        return true
    }

    override fun onSuccessLogout() {
        SharedPrefManager.getInstance(context)?.restoreUser()
        startActivity(Intent(mContext, LoginActivity::class.java))
        (mContext as AppCompatActivity).finish()
    }

    @SuppressLint("SetTextI18n")
    override fun showProfileUser(
        name: String, email: String,
        school: String, address: String, phone: String, datetime: String
    ) {
        tv_name?.text = name
        tv_class?.text = email
        tv_experience?.text = school
        tv_address?.text = address
        tv_phone_number?.text = phone
        tv_datetime?.text = "Terdaftar Sejak: $datetime"
    }

    override fun handleResponse(message: String?) {
        Toast.makeText(
            mContext,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun hideProgressBar() {
        swipe_refresh?.isRefreshing = false
    }

    override fun showProgressBar() {
        swipe_refresh?.isRefreshing = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UtilsConstant.GALLERY_PICK && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            context?.let {
                CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(200, 200)
                    .start(it, this@TeacherProfileFragment)
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            val result = CropImage.getActivityResult(data)

            if (resultCode == Activity.RESULT_OK) {

                presenter.uploadPhotoProfile(result)

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(
                    context, "Crop Image Error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onSuccessUpload(context: Context?, message: String?) {
        presenter.getPhotoFromStorage()
        Toast.makeText(
            context, message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun showPhotoProfile(photoUrl: String) {
        activity?.applicationContext?.let {
            GlideApp.with(it)
                .load(photoUrl)
                .placeholder(R.drawable.profile_placeholder)
                .into(img_profile)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    private fun prepare(view: View) {
        mContext = view.context

        (mContext as AppCompatActivity).setSupportActionBar(toolbar)
        (mContext as AppCompatActivity).supportActionBar?.title = "Profil Pemateri"

        presenter = TeacherProfilePresenter(mContext, this)
    }

    private fun getPhotoFromStorage(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            activity?.let {
                ActivityCompat.requestPermissions(
                    it, arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    UtilsConstant.PERMISSION_STORAGE
                )
            }

        } else {
            val galleryIntent = Intent()
            galleryIntent.type = "image/*"
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(galleryIntent, "SELECT IMAGE"),
                UtilsConstant.GALLERY_PICK
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            UtilsConstant.PERMISSION_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val galleryIntent = Intent()
                    galleryIntent.type = "image/*"
                    galleryIntent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(
                        Intent.createChooser(galleryIntent, "SELECT IMAGE"),
                        UtilsConstant.GALLERY_PICK
                    )
                }
                return
            }
        }
    }
}