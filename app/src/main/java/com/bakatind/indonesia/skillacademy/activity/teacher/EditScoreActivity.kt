package com.bakatind.indonesia.skillacademy.activity.teacher

import android.content.pm.ActivityInfo
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bakatind.indonesia.skillacademy.GlideApp
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.CATEGORY_KEY
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.EMAIL
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.GET_PROFILE
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.IMAGE
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.LESSON_KEY
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.MESSAGE
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.SCORE
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.USERNAME
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_give_score.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class EditScoreActivity : AppCompatActivity() {

    private var mImage: String? = null
    private var mUsername: String? = null
    private var mEmail: String? = null
    private var mScore: Long? = null
    private var mMessage: String? = null
    private var mLessonId: String? = null
    private var mClassId: String? = null
    private var mUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_give_score)
        prepare()
        btn_send.setOnClickListener {
            mMessage = input_message.text.toString()
            mScore = input_score.text.toString().toLong()

            if (mScore.toString() == "null" || mMessage == "null") {
                Toast.makeText(this, "Tidak boleh ada data yang kosong", Toast.LENGTH_SHORT)
                    .show()
            } else {
                when {
                    mScore ?: 0 > 100 -> {
                        Toast.makeText(this, "Nilai tidak boleh lebih dari 100", Toast.LENGTH_SHORT)
                            .show()
                    }
                    mScore ?: 0 < 10 -> {
                        Toast.makeText(this, "Nilai tidak boleh kurang dari 10", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else -> {
                        editData()
                    }
                }
            }
        }
    }

    private fun prepare() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Edit Nilai"

        bindProgressButton(btn_send)
        btn_send.attachTextChangeAnimator()

        mImage = intent.getStringExtra(IMAGE).toString()
        mUsername = intent.getStringExtra(USERNAME).toString()
        mEmail = intent.getStringExtra(EMAIL).toString()
        mLessonId = intent.getStringExtra(LESSON_KEY).toString()
        mClassId = intent.getStringExtra(CATEGORY_KEY).toString()
        mUserId = intent.getStringExtra(GET_PROFILE).toString()
        mScore = intent.getLongExtra(SCORE, 0)
        mMessage = intent.getStringExtra(MESSAGE).toString()

        GlideApp.with(applicationContext)
            .load(mImage)
            .placeholder(R.drawable.profile_placeholder)
            .into(img_profile)

        tv_lesson.text = mUsername
        tv_class.text = mEmail
        input_score.setText(mScore.toString())
        input_message.setText(mMessage)
    }

    private fun editData() {
        btn_send.showProgress { progressColor = Color.WHITE }

        val db = FirebaseFirestore.getInstance()

        val data = HashMap<String, Any?>()
        data["score"] = mScore
        data["message"] = mMessage

        db.collection("student")
            .document(mUserId.toString())
            .collection("score")
            .document(mClassId.toString())
            .update(data)
            .addOnSuccessListener {
                btn_send.hideProgress(R.string.btn_send)
                Toast.makeText(this, "Nilai berhasil diubah", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                btn_send.hideProgress(R.string.btn_send)
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }
}