package com.bakatind.indonesia.skillacademy.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.fragment.DashboardFragment
import com.bakatind.indonesia.skillacademy.fragment.LessonFragment
import com.bakatind.indonesia.skillacademy.fragment.ProfileFragment
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.KEY_FRAGMENT
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URLEncoder

class MainActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {

    private var pageContent: Fragment? = DashboardFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init(savedInstanceState)
        firstMenuSelected()
        checkActiveUser()
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
        progressHorizontal.visibility = View.VISIBLE

        val db = FirebaseFirestore.getInstance()
        db.collection("admin")
            .document("nomor WA")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    progressHorizontal.visibility = View.GONE
                    Toast.makeText(
                        this,
                        error.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    progressHorizontal.visibility = View.GONE

                    val getAdmin = value?.getString("admin")

                    val phoneNumber = getAdmin?.replace("+62", "+62 ")
                    val message = ""

                    val url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" +
                            URLEncoder.encode(message, "UTF-8")

                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    startActivity(i)
                }
            }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        pageContent?.let { supportFragmentManager.putFragment(outState, KEY_FRAGMENT, it) }
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onNavigationItemSelected(menu: MenuItem): Boolean {
        when (menu.itemId) {
            R.id.menu_dashboard -> {
                pageContent = DashboardFragment()
            }
            R.id.menu_lesson -> {
                pageContent = LessonFragment()
            }
            R.id.menu_profile -> {
                pageContent = ProfileFragment()
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
            pageContent = supportFragmentManager.getFragment(savedInstanceState, KEY_FRAGMENT)
            pageContent?.let {
                supportFragmentManager.beginTransaction().replace(R.id.view_pager, it).commit()
            }
        }
    }
}