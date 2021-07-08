package com.bakatind.indonesia.skillacademy.fragment.teacher

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.activity.SubjectListActivity
import com.bakatind.indonesia.skillacademy.adapter.SubCategoryAdapter
import com.bakatind.indonesia.skillacademy.model.CategoryResponse
import com.bakatind.indonesia.skillacademy.util.UtilsConstant
import com.bakatind.indonesia.skillacademy.view.SubClassListener
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_tab_list.*

class ClassListFragment : Fragment(), SubClassListener {

    private var getLesson: String? = null
    private var getLessonKey: String? = null
    private var getCategory: String? = null
    private var getCategoryKey: String? = null
    private lateinit var mIntent: Intent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tab_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepare(view)
    }

    private fun prepare(view: View) {
        mIntent = (view.context as AppCompatActivity).intent

        tv_not_data.text =
            (view.context as AppCompatActivity).getString(R.string.tv_not_teacher_class)

        getLesson = mIntent.getStringExtra(UtilsConstant.LESSON_NAME).toString()
        getLessonKey = mIntent.getStringExtra(UtilsConstant.LESSON_KEY).toString()
        getCategory = mIntent.getStringExtra(UtilsConstant.CATEGORY_NAME).toString()
        getCategoryKey = mIntent.getStringExtra(UtilsConstant.CATEGORY_KEY).toString()

        rv_data_list?.layoutManager = LinearLayoutManager(context)
        rv_data_list?.setHasFixedSize(true)
        swipe_refresh?.isEnabled = false

        setupDatabse()
        getDataCount()

        swipe_refresh?.setOnRefreshListener {
            setupDatabse()
            getDataCount()
        }
    }

    private fun setupDatabse() {
        val query = FirebaseFirestore.getInstance()
            .collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("subCategory")
            .orderBy("date")

        val options = FirestoreRecyclerOptions.Builder<CategoryResponse>()
            .setQuery(query, CategoryResponse::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = SubCategoryAdapter(options, this)
        rv_data_list?.adapter = adapter
    }

    private fun getDataCount() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
            .collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("subCategory")

        db.addSnapshotListener { snapshot, _ ->
            if ((snapshot?.size() ?: 0) > 0) {
                rv_data_list?.visibility = View.VISIBLE
                tv_not_data?.visibility = View.GONE
            } else {
                rv_data_list?.visibility = View.GONE
                tv_not_data?.visibility = View.VISIBLE
            }
            swipe_refresh?.isRefreshing = false
        }
    }

    override fun onClick(key: String, name: String) {
        val intent = Intent(context, SubjectListActivity::class.java)
        intent.putExtra(UtilsConstant.LESSON_NAME, getLesson)
        intent.putExtra(UtilsConstant.LESSON_KEY, getLessonKey)
        intent.putExtra(UtilsConstant.CATEGORY_NAME, getCategory)
        intent.putExtra(UtilsConstant.CATEGORY_KEY, getCategoryKey)
        intent.putExtra(UtilsConstant.SUB_CATEGORY_NAME, name)
        intent.putExtra(UtilsConstant.SUB_CATEGORY_KEY, key)
        startActivity(intent)
    }
}