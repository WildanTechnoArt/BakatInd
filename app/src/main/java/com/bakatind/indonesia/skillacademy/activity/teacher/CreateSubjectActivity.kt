package com.bakatind.indonesia.skillacademy.activity.teacher

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.model.teacher.ClassResponse
import com.bakatind.indonesia.skillacademy.util.UtilsConstant
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.FILE_PICK
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.PERMISSION_STORAGE
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_create_subject.*
import kotlinx.android.synthetic.main.add_link_dialog.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.util.*

class CreateSubjectActivity : AppCompatActivity() {

    private var teacherid: String? = null
    private var mClassId: String? = null
    private var mLessonId: String? = null
    private var mLessonName: String? = null
    private var mClassName: String? = null
    private var getSubCategory: String? = null
    private var getSubCategoryKey: String? = null
    private var getSubjectName: String? = null
    private var getSubjectId: String? = null
    private var mLink: String? = null
    private var mVideo: String? = null
    private var mPdfUri: Uri? = null
    private var mPdfLink: String? = null
    private val fileReference = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_subject)
        init()
    }

    private fun init() {
        setSupportActionBar(toolbar)

        teacherid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Buat Materi Baru"
        }

        mLessonName = intent.getStringExtra(UtilsConstant.LESSON_NAME).toString()
        mLessonId = intent.getStringExtra(UtilsConstant.LESSON_KEY).toString()
        mClassName = intent.getStringExtra(UtilsConstant.CATEGORY_NAME).toString()
        mClassId = intent.getStringExtra(UtilsConstant.CATEGORY_KEY).toString()
        getSubCategory = intent.getStringExtra(UtilsConstant.SUB_CATEGORY_NAME).toString()
        getSubCategoryKey = intent.getStringExtra(UtilsConstant.SUB_CATEGORY_KEY).toString()

        bindProgressButton(btn_create_subject)
        btn_create_subject.attachTextChangeAnimator()

        btn_create_subject.setOnClickListener {
            createClass()
        }

        card_pdf.setOnClickListener {
            getFilePdf()
        }

        card_video.setOnClickListener {
            showDialogInputLink("video")
        }

        card_link.setOnClickListener {
            showDialogInputLink("link")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    val mimeTypes = arrayOf(
                        "application/pdf"
                    )

                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)

                    intent.type = if (mimeTypes.size == 1) mimeTypes[0] else "*/*"
                    if (mimeTypes.isNotEmpty()) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                        }
                    }

                    startActivityForResult(
                        Intent.createChooser(intent, "SELECT FILE"),
                        FILE_PICK
                    )
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_PICK && resultCode == Activity.RESULT_OK) {
            mPdfUri = data?.data
            layout_card_pdf.setBackgroundColor(Color.LTGRAY)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun createClass() {
        getSubjectName = input_subject_name.text.toString()

        if ((mLink.toString() == "null" && mVideo.toString() == "null" && mPdfUri.toString() == "null")
            || getSubjectName.toString() == "null"
        ) {
            Toast.makeText(
                this, "Tidak boleh ada data yang kosong",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val query = FirebaseFirestore.getInstance()

            val data = ClassResponse()
            data.classname = mClassName
            data.lessonname = mLessonName
            data.classid = mClassId
            data.lessonid = mLessonId
            data.link = mLink
            data.video = mVideo
            data.name = getSubjectName
            data.subclassId = getSubCategoryKey
            data.subclassname = getSubCategory
            data.date = Calendar.getInstance().time

            btn_create_subject.showProgress { progressColor = Color.WHITE }

            if (mPdfUri.toString() == "null") {
                query.collection("lessons")
                    .document(mLessonId.toString())
                    .collection("category")
                    .document(mClassId.toString())
                    .collection("subCategory")
                    .document(getSubCategoryKey.toString())
                    .collection("listSub")
                    .document()
                    .set(data)
                    .addOnSuccessListener {
                        saveToClassDatabase()
                    }.addOnFailureListener {
                        btn_create_subject.hideProgress(R.string.btn_create_subject)
                        Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
            } else {
                uploadFilePdf()
            }
        }
    }

    private fun saveToClassDatabase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("lessons")
            .document(mLessonId.toString())
            .collection("category")
            .document(mClassId.toString())
            .update("subclassId", getSubCategoryKey)
            .addOnSuccessListener {
                btn_create_subject.hideProgress(R.string.btn_create_subject)
                hideProgressUpload()
                Toast.makeText(
                    this,
                    "Materi berhasil ditambahkan",
                    Toast.LENGTH_SHORT
                )
                    .show()
                finish()
            }
            .addOnFailureListener {
                btn_create_subject.hideProgress(R.string.btn_create_subject)
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT)
                    .show()
                hideProgressUpload()
            }
    }

    private fun getFilePdf() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this, arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                PERMISSION_STORAGE
            )

        } else {

            val mimeTypes = arrayOf(
                "application/pdf"
            )

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            intent.type = if (mimeTypes.size == 1) mimeTypes[0] else "*/*"
            if (mimeTypes.isNotEmpty()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                }
            }

            startActivityForResult(Intent.createChooser(intent, "SELECT FILE"), FILE_PICK)
        }
    }

    private fun uploadFilePdf() {
        val subject = FirebaseFirestore.getInstance()
        getSubjectId = subject.collection("lessons")
            .document(mLessonId.toString())
            .collection("category")
            .document(mClassId.toString())
            .collection("subCategory")
            .document(getSubCategoryKey.toString())
            .collection("listSub")
            .document()
            .id

        val fileURL = "document_pdf/$getSubjectId" + "_" + "${mPdfUri?.lastPathSegment}"
        val filePath = fileReference.child(fileURL)

        filePath.putFile(mPdfUri!!)
            .addOnProgressListener {
                val progress: Double =
                    100.0 * (it.bytesTransferred / it.totalByteCount)
                val progressBar = progress.toInt()
                showProgressUpload(progressBar)
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    fileReference.child(fileURL).downloadUrl
                        .addOnSuccessListener { imageUri: Uri? ->
                            mPdfLink = imageUri.toString()

                            val data = ClassResponse()
                            data.classname = mClassName
                            data.lessonname = mLessonName
                            data.classid = mClassId
                            data.lessonid = mLessonId
                            data.link = mLink
                            data.video = mVideo
                            data.name = getSubjectName
                            data.subclassId = getSubCategoryKey
                            data.pdf = mPdfLink
                            data.subclassname = getSubCategory
                            data.date = Calendar.getInstance().time

                            val db = FirebaseFirestore.getInstance()
                            db.collection("lessons")
                                .document(mLessonId.toString())
                                .collection("category")
                                .document(mClassId.toString())
                                .collection("subCategory")
                                .document(getSubCategoryKey.toString())
                                .collection("listSub")
                                .document(getSubjectId.toString())
                                .set(data)
                                .addOnSuccessListener {
                                    saveToClassDatabase()
                                }
                                .addOnFailureListener {
                                    btn_create_subject.hideProgress(R.string.btn_create_subject)
                                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT)
                                        .show()
                                    hideProgressUpload()
                                }

                        }.addOnFailureListener {
                            btn_create_subject.hideProgress(R.string.btn_create_subject)
                            Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                            hideProgressUpload()
                        }

                } else {
                    btn_create_subject.hideProgress(R.string.btn_create_subject)
                    Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
                    hideProgressUpload()
                }
            }
    }

    private fun showDialogInputLink(field: String) {
        var alertDialog: AlertDialog? = null

        val builder = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)
        val dialogView = (this as AppCompatActivity).layoutInflater.inflate(
            R.layout.add_link_dialog,
            null
        )

        bindProgressButton(dialogView.btn_add_link)
        dialogView.btn_add_link.attachTextChangeAnimator()

        dialogView.btn_add_link.setOnClickListener {
            if (dialogView.input_link.text.toString().isEmpty()) {
                Toast.makeText(this, "Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
            } else {
                if (field == "video") {
                    Toast.makeText(this, "Video berhasil ditambahkan", Toast.LENGTH_SHORT)
                        .show()
                    mVideo = dialogView.input_link.text.toString()
                    layout_card_video.setBackgroundColor(Color.LTGRAY)
                } else {
                    Toast.makeText(this, "Link berhasil ditambahkan", Toast.LENGTH_SHORT)
                        .show()
                    mLink = dialogView.input_link.text.toString()
                    layout_card_link.setBackgroundColor(Color.LTGRAY)
                }

                dialogView.btn_add_link.hideProgress(R.string.btn_add_link)
                alertDialog?.dismiss()
            }
        }

        dialogView.btn_cancel.setOnClickListener {
            alertDialog?.dismiss()
        }

        builder.setView(dialogView)
        builder.setTitle("Tambah Link")

        alertDialog = builder.create()
        alertDialog.show()
    }

    private fun hideProgressUpload() {
        tv_progress.visibility = View.GONE
        progressHorizontal.visibility = View.GONE
        shadow.visibility = View.GONE
    }

    private fun showProgressUpload(progress: Int) {
        tv_progress.visibility = View.VISIBLE
        progressHorizontal.visibility = View.VISIBLE
        shadow.visibility = View.VISIBLE
        progressHorizontal.progress = progress
    }
}