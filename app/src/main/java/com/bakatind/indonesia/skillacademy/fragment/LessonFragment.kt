package com.bakatind.indonesia.skillacademy.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bakatind.indonesia.skillacademy.R
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.bakatind.indonesia.skillacademy.adapter.LessonAdapter
import com.bakatind.indonesia.skillacademy.model.LessonResponse
import kotlinx.android.synthetic.main.fragment_lesson.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class LessonFragment : Fragment() {

    private var mContext: Context? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lesson, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepare(view)
    }

    private fun prepare(view: View) {
        setHasOptionsMenu(true)
        mContext = view.context

        (mContext as AppCompatActivity).setSupportActionBar(toolbar)
        (mContext as AppCompatActivity).supportActionBar?.title = "Materi Skill"

        rv_lesson?.layoutManager = GridLayoutManager(view.context, 2)
        rv_lesson?.setHasFixedSize(true)

        setupDatabse()
        getDataCount()
        swipe_refresh.setOnRefreshListener {
            setupDatabse()
            getDataCount()
        }
    }

    private fun setupDatabse() {
        val query = FirebaseFirestore.getInstance()
            .collection("lessons")

        val options = FirestoreRecyclerOptions.Builder<LessonResponse>()
            .setQuery(query, LessonResponse::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = LessonAdapter(options)
        rv_lesson?.adapter = adapter
    }

    private fun getDataCount() {
        val db = FirebaseFirestore.getInstance()
            .collection("lessons")

        db.addSnapshotListener { snapshot, _ ->
            swipe_refresh.isRefreshing = false

            if ((snapshot?.size() ?: 0) > 0) {
                rv_lesson?.visibility = View.VISIBLE
                tv_no_data?.visibility = View.GONE
            } else {
                rv_lesson?.visibility = View.GONE
                tv_no_data?.visibility = View.VISIBLE
            }
        }
    }
}