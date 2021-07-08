package com.bakatind.indonesia.skillacademy.activity.teacher

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bakatind.indonesia.skillacademy.GlideApp
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.model.teacher.ClassResponse
import com.bakatind.indonesia.skillacademy.util.UtilsConstant
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_create_class.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class CreateClassActivity : AppCompatActivity() {

    private var teacherid: String? = null
    private var mLessonId: String? = null
    private var mLessonName: String? = null
    private var mLink: String? = null
    private var mClassName: String? = null
    private var imgResult: CropImage.ActivityResult? = null
    private val imageReference = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_class)
        prepare()
        getLessonList()
    }

    private fun prepare() {
        setSupportActionBar(toolbar)

        teacherid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Buat Kelas Baru"
        }

        bindProgressButton(btn_create_class)
        btn_create_class.attachTextChangeAnimator()

        btn_create_class.setOnClickListener {
            createClass()
        }

        fab_image.setOnClickListener {
            getPhotoFromStorage()
        }
    }

    private fun getPhotoFromStorage() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            this.let {
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
        requestCode: Int,
        permissions: Array<out String>,
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UtilsConstant.GALLERY_PICK && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            this.let {
                CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(200, 200)
                    .start(it)
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            imgResult = CropImage.getActivityResult(data)
            applicationContext.let {
                GlideApp.with(it)
                    .load(imgResult?.uri)
                    .into(img_sampul)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun getLessonList() {
        try {
            val rootRef = FirebaseFirestore.getInstance()
            val subjectsRef = rootRef.collection("lessons")
            val lessonList: MutableList<String?> = ArrayList()
            val lessonIdList: MutableList<String?> = ArrayList()
            val adapter =
                ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, lessonList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            input_lesson.setAdapter(adapter)
            input_lesson.setOnItemClickListener { _, _, position, _ ->
                mLessonId = lessonIdList[position].toString()
            }

            subjectsRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val lessonName = document.getString("lesson")
                        lessonList.add(lessonName)
                        lessonIdList.add(document.id)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun createClass() {
        mLessonName = input_lesson.text.toString()
        mClassName = input_class.text.toString()
        mLink = input_link.text.toString()

        if (mLessonId.toString() == "null" || mLessonName.toString() == "null"
            || mClassName.toString() == "null" || imgResult == null || mLink.toString() == "null"
        ) {
            Toast.makeText(
                this, "Tidak boleh ada data yang kosong",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            btn_create_class.showProgress { progressColor = Color.WHITE }

            val resultUri = imgResult?.uri

            val thumbImage = File(resultUri?.path.toString())

            val thumbBitmap = Compressor(this)
                .setMaxHeight(250)
                .setMaxWidth(250)
                .setQuality(100)
                .compressToBitmap(thumbImage)

            val bios = ByteArrayOutputStream()
            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bios)
            val imageByte = bios.toByteArray()

            val thumbURL = "class_image/${UUID.randomUUID()}.jpg"
            val thumbPath = imageReference.child(thumbURL)

            resultUri?.let { it1 ->
                thumbPath.putFile(it1).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        imageReference.child(thumbURL).downloadUrl.addOnSuccessListener { imageUri: Uri? ->

                            val uploadTask: UploadTask = thumbPath.putBytes(imageByte)

                            uploadTask.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val imageUrl = imageUri.toString()
                                    saveToDatabase(imageUrl)
                                } else {
                                    btn_create_class.hideProgress(R.string.btn_create_class)
                                    Toast.makeText(
                                        this,
                                        task.exception?.localizedMessage.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        }.addOnFailureListener {
                            btn_create_class.hideProgress(R.string.btn_create_class)
                            Toast.makeText(
                                this,
                                it.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        btn_create_class.hideProgress(R.string.btn_create_class)
                        Toast.makeText(
                            this,
                            task.exception?.localizedMessage.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun saveToDatabase(imageUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val classId =  db.collection("teacher")
            .document(teacherid.toString())
            .collection("classList")
            .document()
            .id

        val data = ClassResponse()
        data.classname = mClassName
        data.lessonname = mLessonName
        data.lessonid = mLessonId
        data.classid = classId
        data.image = imageUrl
        data.subscribe = true
        data.link = mLink
        data.date = Calendar.getInstance().time
        data.teacherid = FirebaseAuth.getInstance().currentUser?.uid

        val query = FirebaseFirestore.getInstance()
        query.collection("teacher")
            .document(teacherid.toString())
            .collection("classList")
            .document(classId)
            .set(data)
            .addOnSuccessListener {
                btn_create_class.hideProgress(R.string.btn_create_class)
                Toast.makeText(
                    this,
                    "Kelas berhasil dibuat",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }.addOnFailureListener {
                btn_create_class.hideProgress(R.string.btn_create_class)
                Toast.makeText(
                    this,
                    it.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}