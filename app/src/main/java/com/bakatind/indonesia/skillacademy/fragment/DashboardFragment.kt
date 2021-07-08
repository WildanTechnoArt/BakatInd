package com.bakatind.indonesia.skillacademy.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bakatind.indonesia.skillacademy.GlideApp
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.adapter.FirestoreClassSubsAdapter
import com.bakatind.indonesia.skillacademy.adapter.FirestoreTaskAdapter
import com.bakatind.indonesia.skillacademy.model.TaskResponse
import com.bakatind.indonesia.skillacademy.model.teacher.SubClassResponse
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.synnapps.carouselview.ImageListener
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class DashboardFragment : Fragment() {

    private var mContext: Context? = null
    private lateinit var slideImageListener: ImageListener
    private val slideImageList = arrayListOf<String>()
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepare(view)
        getSlideImageUrl(view.context.applicationContext)
        checkSubscribe()
        checkTask()
        swipe_refresh?.setOnRefreshListener {
            getSlideImageUrl(view.context.applicationContext)
            checkSubscribe()
            checkTask()
        }
    }

    private fun prepare(view: View) {
        setHasOptionsMenu(true)
        mContext = view.context

        userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        (mContext as AppCompatActivity).setSupportActionBar(toolbar)
        (mContext as AppCompatActivity).supportActionBar?.title = "Dashboard"
        swipe_refresh.isEnabled = false
    }

    private fun getSlideImageUrl(context: Context) {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("imageSlider")
            .get()
            .addOnSuccessListener {
                swipe_refresh?.isRefreshing = false

                slideImageList.clear()

                var a = 0
                while (a < it.documents.size) {
                    slideImageList.add(it.documents[a].getString("imageUrl").toString())
                    a++
                }

                slideImageListener = ImageListener { position, imageView ->
                    GlideApp.with(context)
                        .load(slideImageList[position])
                        .centerCrop()
                        .into(imageView)
                }

                carouselView.setImageListener(slideImageListener)
                carouselView.pageCount = slideImageList.size
            }
            .addOnFailureListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun requestSubscribeData() {
        val query = FirebaseFirestore.getInstance()
            .collection("subscribe")
            .document(userId.toString())
            .collection("class")

        val options = FirestoreRecyclerOptions.Builder<SubClassResponse>()
            .setQuery(query, SubClassResponse::class.java)
            .setLifecycleOwner(this)
            .build()

        rv_subscribe_class?.layoutManager = LinearLayoutManager(context)
        rv_subscribe_class?.setHasFixedSize(true)

        val adapter = FirestoreClassSubsAdapter(options)
        rv_subscribe_class?.adapter = adapter
    }

    private fun checkSubscribe() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("subscribe")
            .document(userId.toString())
            .collection("class")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot?.isEmpty == true) {
                    tv_subscribe_class?.visibility = View.VISIBLE
                    rv_subscribe_class?.visibility = View.GONE
                } else {
                    tv_subscribe_class?.visibility = View.GONE
                    rv_subscribe_class?.visibility = View.VISIBLE
                    requestSubscribeData()
                }

                swipe_refresh?.isRefreshing = false
            }
    }

    private fun requestTaskData() {
        val query = FirebaseFirestore.getInstance()
            .collection("student")
            .document(userId.toString())
            .collection("score")

        val options = FirestoreRecyclerOptions.Builder<TaskResponse>()
            .setQuery(query, TaskResponse::class.java)
            .setLifecycleOwner(this)
            .build()

        rv_progress_study?.layoutManager = LinearLayoutManager(context)
        rv_progress_study?.setHasFixedSize(true)

        val adapter = FirestoreTaskAdapter(options)
        rv_progress_study?.adapter = adapter
    }

    private fun checkTask() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("student")
            .document(userId.toString())
            .collection("score")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot?.isEmpty == true) {
                    tv_progress_study?.visibility = View.VISIBLE
                    rv_progress_study?.visibility = View.GONE
                } else {
                    tv_progress_study?.visibility = View.GONE
                    rv_progress_study?.visibility = View.VISIBLE
                    requestTaskData()
                }

                swipe_refresh?.isRefreshing = false
            }
    }
}