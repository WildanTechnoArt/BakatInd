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
import com.bakatind.indonesia.skillacademy.adapter.ReviewListAdapter
import com.bakatind.indonesia.skillacademy.model.ReviewModel
import com.bakatind.indonesia.skillacademy.util.UtilsConstant
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_tab_list.*

class ReviewListFragment : Fragment() {

    private var getLessonKey: String? = null
    private var getCategoryKey: String? = null
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
        checkClass()
    }

    private fun prepare(view: View) {
        mIntent = (view.context as AppCompatActivity).intent

        tv_not_data.text =
            (view.context as AppCompatActivity).getString(R.string.tv_no_review)

        getLessonKey = mIntent.getStringExtra(UtilsConstant.LESSON_KEY).toString()
        getCategoryKey = mIntent.getStringExtra(UtilsConstant.CATEGORY_KEY).toString()

        swipe_refresh?.setOnRefreshListener {
            checkClass()
        }
    }

    private fun requestData() {
        val query = FirebaseFirestore.getInstance()
            .collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("reviewList")

        val options = FirestoreRecyclerOptions.Builder<ReviewModel>()
            .setQuery(query, ReviewModel::class.java)
            .setLifecycleOwner(this)
            .build()

        rv_data_list?.layoutManager = LinearLayoutManager(context)
        rv_data_list?.setHasFixedSize(true)

        val adapter = context?.applicationContext?.let { ReviewListAdapter(options, it) }
        rv_data_list?.adapter = adapter
    }

    private fun checkClass() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("reviewList")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot?.isEmpty == true) {
                    tv_not_data?.visibility = View.VISIBLE
                    rv_data_list?.visibility = View.GONE
                } else {
                    tv_not_data?.visibility = View.GONE
                    rv_data_list?.visibility = View.VISIBLE
                    requestData()
                }

                swipe_refresh?.isRefreshing = false
            }
    }
}