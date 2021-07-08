package com.bakatind.indonesia.skillacademy.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.onesignal.OneSignal
import com.bakatind.indonesia.skillacademy.GlideApp
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.activity.teacher.TeacherMainActivity
import com.bakatind.indonesia.skillacademy.database.SharedPrefManager
import com.bakatind.indonesia.skillacademy.fcm.NotificationOpenedHandler
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.PLAY_SERVICES_RESOLUTION_REQUEST
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener
    private lateinit var mAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        checkPlayServices()
        prepare()
        screenProgress()
    }

    private fun prepare() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        mAnalytics = FirebaseAnalytics.getInstance(this)
        mAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
        mAuth = FirebaseAuth.getInstance()

        GlideApp.with(applicationContext)
            .load(R.drawable.logo_bakain_transparan)
            .into(img_app_name)

        OneSignal.initWithContext(this)
        OneSignal.setAppId("e0a91788-5a3a-4613-b947-adbd306949e3")
        OneSignal.unsubscribeWhenNotificationsAreDisabled(false)
        OneSignal.setNotificationOpenedHandler(NotificationOpenedHandler(this))
    }

    private fun screenProgress() {
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                val userId = user.uid

                val db = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()

                db.addOnSuccessListener { result ->
                    val getEmail = result.getString("email").toString()
                    SharedPrefManager.getInstance(this)?.setEmailUser(getEmail)
                    if (result.getBoolean("teacher") == true) {
                        SharedPrefManager.getInstance(this)?.isTeacher(true)
                        startActivity(Intent(this, TeacherMainActivity::class.java))
                        finishAffinity()
                    } else {
                        SharedPrefManager.getInstance(this)?.isTeacher(false)
                        startActivity(Intent(this, MainActivity::class.java))
                        finishAffinity()
                    }
                }
            } else {
                val thread = object : Thread() {
                    override fun run() {
                        try {
                            sleep(1500)
                        } catch (ex: InterruptedException) {
                            ex.printStackTrace()
                        } finally {
                            startActivity(Intent(applicationContext, LoginActivity::class.java))
                            finish()
                        }
                    }
                }
                thread.start()
            }
        }
    }

    private fun checkPlayServices() {
        val googleAPI = GoogleApiAvailability.getInstance()
        val result = googleAPI.isGooglePlayServicesAvailable(this)
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(
                    this, result,
                    PLAY_SERVICES_RESOLUTION_REQUEST
                ).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(mAuthListener)
    }
}