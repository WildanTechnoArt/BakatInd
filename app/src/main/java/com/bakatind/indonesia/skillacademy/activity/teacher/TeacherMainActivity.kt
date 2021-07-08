package com.bakatind.indonesia.skillacademy.activity.teacher

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.fragment.LessonFragment
import com.bakatind.indonesia.skillacademy.fragment.teacher.TeacherDashboardFragment
import com.bakatind.indonesia.skillacademy.fragment.teacher.TeacherProfileFragment
import com.bakatind.indonesia.skillacademy.util.UtilsConstant
import com.firebase.ui.auth.AuthUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_teacher_main.*
import java.net.URLEncoder

class TeacherMainActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {

    private var pageContent: Fragment? = TeacherDashboardFragment()
    private val database = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_main)
        init(savedInstanceState)
        firstMenuSelected()
        checkActiveUser()
        checkApprove()
    }

    private fun checkApprove() {
        progressBar.visibility = VISIBLE

        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userId)
            .addSnapshotListener { value, _ ->
                progressBar.visibility = GONE

                val isApprove = value?.getBoolean("approve")
                if (isApprove == true) {
                    btn_request.visibility = GONE
                    tv_information.visibility = GONE
                    btn_logout.visibility = GONE
                    view_pager.visibility = VISIBLE
                    bottom_navigation.visibility = VISIBLE
                } else {
                    btn_request.visibility = VISIBLE
                    tv_information.visibility = VISIBLE
                    btn_logout.visibility = VISIBLE
                    view_pager.visibility = GONE
                    bottom_navigation.visibility = GONE

                    btn_request.setOnClickListener {
                        chatAdmin()
                    }
                    btn_logout.setOnClickListener {
                        val alert = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)
                            .setTitle("Konfirmasi")
                            .setMessage("Anda yakin ingin keluar?")
                            .setPositiveButton("Ya") { _, _ ->
                                database.collection("users")
                                    .document(userId)
                                    .update("device", FieldValue.increment(-1))
                                    .addOnSuccessListener {
                                        AuthUI.getInstance()
                                            .signOut(this)
                                            .addOnCompleteListener {
                                                if (it.isSuccessful) {
                                                    finish()
                                                } else {
                                                    Toast.makeText(
                                                        this,
                                                        "Logout gagal, silakan coba lagi",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            this,
                                            it.localizedMessage?.toString(),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                            .setNegativeButton("Tidak") { dialog, _ ->
                                dialog.dismiss()
                            }
                        alert.create()
                        alert.show()
                    }
                }
            }
    }

    private fun checkActiveUser() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userId)
            .addSnapshotListener { value, _ ->
                val isActive = value?.getBoolean("active")
                if ((isActive == false) || value?.exists() == false) {
                    showAlertDialog(getString(R.string.policy_login))
                }
            }
    }

    private fun showAlertDialog(message: String) {
        val builder = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)
            .setMessage(message)
            .setPositiveButton("Hubungi Admin") { _, _ ->
                chatAdmin()
                finish()
            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun chatAdmin() {
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

                    val getAdmin = value?.getString("admin")

                    val phoneNumber = getAdmin?.replace("+62", "+62 ")

                    val url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" +
                            URLEncoder.encode("", "UTF-8")

                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    startActivity(i)
                }
            }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        pageContent?.let {
            supportFragmentManager.putFragment(
                outState,
                UtilsConstant.KEY_FRAGMENT, it
            )
        }
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onNavigationItemSelected(menu: MenuItem): Boolean {
        when (menu.itemId) {
            R.id.menu_class -> {
                pageContent = TeacherDashboardFragment()
            }
            R.id.menu_lesson -> {
                pageContent = LessonFragment()
            }
            R.id.menu_profile -> {
                pageContent = TeacherProfileFragment()
            }
        }
        pageContent?.let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.view_pager, it)
                .commit()
        }
        return true
    }

    private fun firstMenuSelected() {
        pageContent?.let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.view_pager, it)
                .commit()
        }
    }

    private fun init(savedInstanceState: Bundle?) {
        bottom_navigation.setOnNavigationItemSelectedListener(this)
        if (savedInstanceState == null) {
            pageContent?.let {
                supportFragmentManager.beginTransaction().replace(R.id.view_pager, it).commit()
            }
        } else {
            pageContent = supportFragmentManager.getFragment(
                savedInstanceState,
                UtilsConstant.KEY_FRAGMENT
            )
            pageContent?.let {
                supportFragmentManager.beginTransaction().replace(R.id.view_pager, it).commit()
            }
        }
    }
}