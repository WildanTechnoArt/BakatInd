package com.bakatind.indonesia.skillacademy.activity.student

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.model.ReviewModel
import com.bakatind.indonesia.skillacademy.util.UtilsConstant
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_review.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class ReviewActivity : AppCompatActivity() {

    private var getClassName: String? = null
    private var userid: String? = null
    private var rating: Float? = null
    private var review: String? = null
    private var mClassId: String? = null
    private var mLessonId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)
        prepare()
    }

    @SuppressLint("SetTextI18n")
    private fun prepare() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        userid = FirebaseAuth.getInstance().currentUser?.uid.toString()

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Tulis Ulasan"

        mLessonId = intent.getStringExtra(UtilsConstant.LESSON_KEY).toString()
        mClassId = intent.getStringExtra(UtilsConstant.CATEGORY_KEY).toString()
        getClassName = intent.getStringExtra(UtilsConstant.CATEGORY_NAME).toString()

        tv_review.text = "${getString(R.string.tv_review)} $getClassName"

        bindProgressButton(btn_submit)
        btn_submit.attachTextChangeAnimator()

        btn_submit.setOnClickListener {
            sendReview()
        }
    }

    private fun sendReview() {
        review = input_review.text.toString()
        rating = ratingBar.rating

        if (review == "null" || rating.toString() == "null") {
            Toast.makeText(this, "Silakan masukan rating dan ulasan anda", Toast.LENGTH_SHORT)
                .show()
        } else {
            btn_submit.showProgress { progressColor = Color.WHITE }

            val data = ReviewModel()
            data.userid = userid
            data.rating = rating?.toInt()
            data.review = review

            val query = FirebaseFirestore.getInstance()
            query.collection("lessons")
                .document(mLessonId.toString())
                .collection("category")
                .document(mClassId.toString())
                .collection("reviewList")
                .document()
                .set(data)
                .addOnSuccessListener {
                    btn_submit.hideProgress(R.string.btn_submit)
                    Toast.makeText(this, "Terimakasih atas ulasan anda", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }.addOnFailureListener {
                    btn_submit.hideProgress(R.string.btn_submit)
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        }
    }
}