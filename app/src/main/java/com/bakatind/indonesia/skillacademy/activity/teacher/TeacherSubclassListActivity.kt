package com.bakatind.indonesia.skillacademy.activity.teacher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.adapter.teacher.PagerAdapterSubject
import com.bakatind.indonesia.skillacademy.database.SharedPrefManager
import com.bakatind.indonesia.skillacademy.model.teacher.SubClassResponse
import com.bakatind.indonesia.skillacademy.util.UtilsConstant
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.FAB_BROADCAST
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_subclass_list.*
import kotlinx.android.synthetic.main.activity_subclass_list.tabs
import kotlinx.android.synthetic.main.activity_subclass_list.viewpager
import kotlinx.android.synthetic.main.add_class_dialog.view.*
import kotlinx.android.synthetic.main.toolbar_layout.toolbar
import java.net.URLEncoder
import java.util.*

class TeacherSubclassListActivity : AppCompatActivity() {

    private var getCategory: String? = null
    private var isTeacher: Boolean? = null
    private var getLesson: String? = null
    private var getLessonKey: String? = null
    private var getCategoryKey: String? = null
    private var subclassName: String? = null
    private var teacherid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subclass_list)
        prepare()
    }

    private fun prepare() {
        getLesson = intent.getStringExtra(UtilsConstant.LESSON_NAME).toString()
        getLessonKey = intent.getStringExtra(UtilsConstant.LESSON_KEY).toString()
        getCategory = intent.getStringExtra(UtilsConstant.CATEGORY_NAME).toString()
        getCategoryKey = intent.getStringExtra(UtilsConstant.CATEGORY_KEY).toString()

        isTeacher = SharedPrefManager.getInstance(this)?.getIsTeacher
        teacherid = FirebaseAuth.getInstance().currentUser?.uid.toString()

        setSupportActionBar(toolbar)
        supportActionBar?.title = getCategory

        val tabMenus = arrayOf(
            getString(R.string.subclass_list),
            getString(R.string.student_list),
            getString(R.string.review)
        )

        val pageAdapter = PagerAdapterSubject(this)

        viewpager.adapter = pageAdapter

        TabLayoutMediator(
            tabs,
            viewpager
        ) { tab, position ->
            tab.text = tabMenus[position]
        }.attach()

        if (isTeacher == true) {
            fab_subclass.visibility = VISIBLE
        }

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> fab_subclass.show()
                    else -> fab_subclass.hide()
                }
            }

        })

        fab_subclass.setOnClickListener {
            addNewSubClass()
        }

        getFabBroadcast()
    }

    private fun getFabBroadcast() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val getCondition = intent?.getBooleanExtra("endScroll", false)
                if (getCondition == true) {
                    fab_subclass.hide()
                } else {
                    fab_subclass.show()
                }
            }
        }

        val intentFilter = IntentFilter(FAB_BROADCAST)
        registerReceiver(receiver, intentFilter)
    }

    private fun addNewSubClass() {
        var alertDialog: AlertDialog? = null

        val builder = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)
        val dialogView = (this as AppCompatActivity).layoutInflater.inflate(
            R.layout.add_class_dialog,
            null
        )

        bindProgressButton(dialogView.btn_accept)
        dialogView.btn_accept.attachTextChangeAnimator()

        dialogView.btn_accept.setOnClickListener {
            subclassName = dialogView.input_class_name.text.toString()
            if (subclassName?.isEmpty() == true) {
                Toast.makeText(this, "Tidak boleh kosong", Toast.LENGTH_SHORT).show()
            } else {
                dialogView.btn_accept.showProgress { progressColor = Color.WHITE }

                val data = SubClassResponse()
                data.name = subclassName
                data.date = Calendar.getInstance().time
                data.classname = getCategory
                data.lessonname = getLesson
                data.lessonid = getLessonKey
                data.classid = getCategoryKey
                data.approve = false

                val db = FirebaseFirestore.getInstance()
                db.collection("teacher")
                    .document(teacherid.toString())
                    .collection("classList")
                    .document(getCategoryKey.toString())
                    .collection("subCategory")
                    .document()
                    .set(data)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Sub Kelas berhasil ditambahkan", Toast.LENGTH_SHORT)
                            .show()
                        dialogView.btn_accept.hideProgress(R.string.btn_save)
                        alertDialog?.dismiss()
                        showAlertApprove()
                    }
                    .addOnFailureListener {
                        dialogView.btn_accept.hideProgress(R.string.btn_save)
                        Toast.makeText(this, it.localizedMessage?.toString(), Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }

        dialogView.btn_cancel.setOnClickListener {
            alertDialog?.dismiss()
        }

        builder.setView(dialogView)
        builder.setTitle("Tambah Sub Kelas Baru")

        alertDialog = builder.create()
        alertDialog.show()
    }

    private fun showAlertApprove() {
        val builder = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)
            .setTitle("Minta Persetujuan")
            .setMessage("Agar kelas anda dapat dipublish silakan minta persetujuan Admin")
            .setPositiveButton("Ok") { _, _ ->
                chatAdmin()
            }
            .setNegativeButton("Lain Kali") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun chatAdmin() {
        val message = """
            Hallo Admin Bakatind 
            Saya ingin meminta persetujuan kelas dengan detail:
            Nama Saya:
            Email: ${SharedPrefManager.getInstance(this)?.getEmailUser.toString()}
            Materi Skill: $getLesson
            Kelas: $getCategory
            Sub Kelas: $subclassName

            Terimaksih.
        """.trimIndent()

        val db = FirebaseFirestore.getInstance()
        db.collection("admin")
            .document("nomor WA")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(
                        this,
                        error.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val getAdmin = value?.getString("admin")

                    val phoneNumber = getAdmin?.replace("+62", "+62 ")

                    val url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" +
                            URLEncoder.encode(message, "UTF-8")

                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    startActivity(i)
                }
            }
    }
}