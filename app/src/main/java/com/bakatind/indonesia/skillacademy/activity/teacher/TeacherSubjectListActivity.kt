package com.bakatind.indonesia.skillacademy.activity.teacher

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bakatind.indonesia.skillacademy.R
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.bakatind.indonesia.skillacademy.adapter.teacher.SubjectListAdapter
import com.bakatind.indonesia.skillacademy.model.teacher.ClassResponse
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.CATEGORY_KEY
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.CATEGORY_NAME
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.LESSON_KEY
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.LESSON_NAME
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.SUB_CATEGORY_KEY
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.SUB_CATEGORY_NAME
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_teacher_subject_list.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class TeacherSubjectListActivity : AppCompatActivity() {

    private var getLesson: String? = null
    private var getLessonKey: String? = null
    private var getCategory: String? = null
    private var getCategoryKey: String? = null
    private var getSubCategory: String? = null
    private var getSubCategoryKey: String? = null
    private var teacherId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_subject_list)
        prepare()
    }

    private fun prepare() {
        setSupportActionBar(toolbar)

        getLesson = intent.getStringExtra(LESSON_NAME).toString()
        getLessonKey = intent.getStringExtra(LESSON_KEY).toString()
        getCategory = intent.getStringExtra(CATEGORY_NAME).toString()
        getCategoryKey = intent.getStringExtra(CATEGORY_KEY).toString()
        getSubCategory = intent.getStringExtra(SUB_CATEGORY_NAME).toString()
        getSubCategoryKey = intent.getStringExtra(SUB_CATEGORY_KEY).toString()
        teacherId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        supportActionBar?.title = getSubCategory

        rv_subject?.layoutManager = LinearLayoutManager(this)
        rv_subject?.setHasFixedSize(true)

        setupDatabse()
        getDataCount()
        swipe_refresh?.setOnRefreshListener {
            setupDatabse()
            getDataCount()
        }

        fab_add_subject.setOnClickListener {
            val intent = Intent(this, CreateSubjectActivity::class.java)
            intent.putExtra(LESSON_NAME, getLesson)
            intent.putExtra(LESSON_KEY, getLessonKey)
            intent.putExtra(CATEGORY_NAME, getCategory)
            intent.putExtra(CATEGORY_KEY, getCategoryKey)
            intent.putExtra(SUB_CATEGORY_NAME, getSubCategory)
            intent.putExtra(SUB_CATEGORY_KEY, getSubCategoryKey)
            startActivity(intent)
        }
    }

    private fun setupDatabse() {
        swipe_refresh?.isRefreshing = true
        val query = FirebaseFirestore.getInstance()
            .collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("subCategory")
            .document(getSubCategoryKey.toString())
            .collection("listSub")
            .orderBy("date")

        val options = FirestoreRecyclerOptions.Builder<ClassResponse>()
            .setQuery(query, ClassResponse::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = SubjectListAdapter(options)
        rv_subject?.adapter = adapter
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
                rv_subject?.visibility = View.VISIBLE
                tv_no_data?.visibility = View.GONE
            } else {
                rv_subject?.visibility = View.GONE
                tv_no_data?.visibility = View.VISIBLE
            }

            swipe_refresh?.isRefreshing = false
        }
    }
}