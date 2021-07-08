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
import com.bakatind.indonesia.skillacademy.adapter.StudentListAdapter
import com.bakatind.indonesia.skillacademy.model.StudentModel
import com.bakatind.indonesia.skillacademy.util.UtilsConstant
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_tab_list.*

class StudentListFragment : Fragment() {

    private var getLessonKey: String? = null
    private var getCategoryKey: String? = null
    private var getTeacherId: String? = null
    private lateinit var mIntent: Intent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepare(view)
        checkStudent()
        requestData()
    }

    private fun prepare(view: View) {
        mIntent = (view.context as AppCompatActivity).intent

        tv_not_data.text =
            (view.context as AppCompatActivity).getString(R.string.tv_no_student)

        getLessonKey = mIntent.getStringExtra(UtilsConstant.LESSON_KEY).toString()
        getCategoryKey = mIntent.getStringExtra(UtilsConstant.CATEGORY_KEY).toString()
        getTeacherId = mIntent.getStringExtra(UtilsConstant.TEACHER_ID).toString()

        swipe_refresh?.isEnabled = false
    }

    private fun requestData() {
        val query = FirebaseFirestore.getInstance()
            .collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("studentList")
            .orderBy("username")

        val options = FirestoreRecyclerOptions.Builder<StudentModel>()
            .setQuery(query, StudentModel::class.java)
            .setLifecycleOwner(this)
            .build()

        rv_data_list?.layoutManager = LinearLayoutManager(context)
        rv_data_list?.setHasFixedSize(true)

        val adapter = StudentListAdapter(options, getTeacherId.toString())
        rv_data_list?.adapter = adapter
    }

    private fun checkStudent() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("studentList")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot?.isEmpty == true) {
                    tv_not_data?.visibility = View.VISIBLE
                    rv_data_list?.visibility = View.GONE
                } else {
                    tv_not_data?.visibility = View.GONE
                    rv_data_list?.visibility = View.VISIBLE
                }

                swipe_refresh?.isRefreshing = false
            }
    }
}