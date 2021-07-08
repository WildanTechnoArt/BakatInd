package com.bakatind.indonesia.skillacademy.activity

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bakatind.indonesia.skillacademy.R
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.bakatind.indonesia.skillacademy.adapter.SubCategoryListAdapter
import com.bakatind.indonesia.skillacademy.database.SharedPrefManager
import com.bakatind.indonesia.skillacademy.model.CategoryResponse
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.CATEGORY_KEY
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.CATEGORY_NAME
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.LESSON_KEY
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.LESSON_NAME
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.SUB_CATEGORY_KEY
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.SUB_CATEGORY_NAME
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class SubjectListActivity : AppCompatActivity() {

    private var getLesson: String? = null
    private var getLessonKey: String? = null
    private var getCategory: String? = null
    private var getCategoryKey: String? = null
    private var getSubCategory: String? = null
    private var getSubCategoryKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)
        prepare()
    }

    private fun prepare() {
        setSupportActionBar(toolbar)

        val isTeacher = SharedPrefManager.getInstance(this)?.getIsTeacher
        if (isTeacher != true) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        getLesson = intent.getStringExtra(LESSON_NAME).toString()
        getLessonKey = intent.getStringExtra(LESSON_KEY).toString()
        getCategory = intent.getStringExtra(CATEGORY_NAME).toString()
        getCategoryKey = intent.getStringExtra(CATEGORY_KEY).toString()
        getSubCategory = intent.getStringExtra(SUB_CATEGORY_NAME).toString()
        getSubCategoryKey = intent.getStringExtra(SUB_CATEGORY_KEY).toString()

        supportActionBar?.title = getSubCategory

        rv_category?.layoutManager = LinearLayoutManager(this)
        rv_category?.setHasFixedSize(true)

        setupDatabse()
        getDataCount()
    }

    private fun setupDatabse() {
        val query = FirebaseFirestore.getInstance()
            .collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("subCategory")
            .document(getSubCategoryKey.toString())
            .collection("listSub")
            .orderBy("date")

        val options = FirestoreRecyclerOptions.Builder<CategoryResponse>()
            .setQuery(query, CategoryResponse::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = SubCategoryListAdapter(options)
        rv_category?.adapter = adapter
    }

    private fun getDataCount() {
        val db = FirebaseFirestore.getInstance()
            .collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("subCategory")
            .document(getSubCategoryKey.toString())
            .collection("listSub")

        db.addSnapshotListener { snapshot, _ ->
            if ((snapshot?.size() ?: 0) > 0) {
                rv_category?.visibility = View.VISIBLE
                tv_no_data?.visibility = View.GONE
            } else {
                rv_category?.visibility = View.GONE
                tv_no_data?.visibility = View.VISIBLE
            }
        }
    }
}