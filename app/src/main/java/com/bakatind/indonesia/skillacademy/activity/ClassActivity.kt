package com.bakatind.indonesia.skillacademy.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bakatind.indonesia.skillacademy.R
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.bakatind.indonesia.skillacademy.view.CategoryListener
import com.bakatind.indonesia.skillacademy.adapter.ClassAdapter
import com.bakatind.indonesia.skillacademy.database.SharedPrefManager
import com.bakatind.indonesia.skillacademy.model.CategoryResponse
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.CATEGORY_KEY
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.CATEGORY_NAME
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.LESSON_KEY
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.LESSON_NAME
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.LINK_TASK
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.TEACHER_ID
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.net.URLEncoder

class ClassActivity : AppCompatActivity(), CategoryListener {

    private var getLesson: String? = null
    private var getLessonKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)
        prepare()
    }

    private fun prepare() {
        setSupportActionBar(toolbar)

        val isTeacher = SharedPrefManager.getInstance(this)?.getIsTeacher
        if (isTeacher == false) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        getLesson = intent.getStringExtra(LESSON_NAME).toString()
        getLessonKey = intent.getStringExtra(LESSON_KEY).toString()

        supportActionBar?.title = getLesson

        rv_category?.layoutManager = GridLayoutManager(this, 2)
        rv_category?.setHasFixedSize(true)

        setupDatabse()
        getDataCount()
    }

    private fun setupDatabse() {
        val query = FirebaseFirestore.getInstance()
            .collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .orderBy("date")

        val options = FirestoreRecyclerOptions.Builder<CategoryResponse>()
            .setQuery(query, CategoryResponse::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = ClassAdapter(options, this)
        rv_category?.adapter = adapter
    }

    private fun getDataCount() {
        val db = FirebaseFirestore.getInstance()
            .collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")

        db.addSnapshotListener { snapshot, _ ->
            if ((snapshot?.size() ?: 0) > 0) {
                rv_category?.visibility = VISIBLE
                tv_no_data?.visibility = GONE
            } else {
                rv_category?.visibility = GONE
                tv_no_data?.visibility = VISIBLE
            }
        }
    }

    override fun onClick(key: String, name: String, teacherId: String, link: String) {
        val intent = Intent(this, SubCategoryActivity::class.java)
        intent.putExtra(LESSON_NAME, getLesson)
        intent.putExtra(LESSON_KEY, getLessonKey)
        intent.putExtra(CATEGORY_NAME, name)
        intent.putExtra(CATEGORY_KEY, key)
        intent.putExtra(TEACHER_ID, teacherId)
        intent.putExtra(LINK_TASK, link)
        startActivity(intent)
    }

    override fun onSubscribe(className: String) {
        val alert = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)
            .setTitle("Pemberitahuan")
            .setMessage(getString(R.string.subscribe_message))
            .setPositiveButton(
                "Berlangganan"
            ) { _, _ ->
                chatAdmin(className)
            }
            .setNegativeButton("Lain Kali") { dialog, _ ->
                dialog.dismiss()
            }
        alert.create()
        alert.show()
    }

    private fun chatAdmin(className: String) {
        progressBar.visibility = VISIBLE

        val db = FirebaseFirestore.getInstance()
        db.collection("admin")
            .document("nomor WA")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    progressBar.visibility = GONE
                    Toast.makeText(
                        this,
                        error.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    progressBar.visibility = GONE

                    val getAccounting = value?.getString("accounting")

                    val phoneNumber = getAccounting?.replace("+62", "+62 ")
                    val message = """
                Hallo Admin Bakatind 
                Saya ingin berlangganan aplikasi Bakatind dengan detail:
                Nama:
                Alamat:
                Umur:
                Email: ${SharedPrefManager.getInstance(this)?.getEmailUser.toString()}

                Saya Ingin mengakses:
                Materi Skill: $getLesson
                Kelas: $className

                Terimaksih.
            """.trimIndent()

                    val url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" +
                            URLEncoder.encode(message, "UTF-8")

                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    startActivity(i)
                }
            }
    }
}