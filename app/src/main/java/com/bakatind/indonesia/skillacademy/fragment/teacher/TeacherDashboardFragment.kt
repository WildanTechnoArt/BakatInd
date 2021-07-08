package com.bakatind.indonesia.skillacademy.fragment.teacher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.activity.teacher.CreateClassActivity
import com.bakatind.indonesia.skillacademy.adapter.teacher.ClassListAdapter
import com.bakatind.indonesia.skillacademy.model.teacher.ClassResponse
import com.bakatind.indonesia.skillacademy.view.ClassListListener
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_teacher_dashboard.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class TeacherDashboardFragment : Fragment(), ClassListListener {

    private var mContext: Context? = null
    private var mUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_teacher_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepare(view)
        fab_new_class.setOnClickListener {
            startActivity(Intent(context, CreateClassActivity::class.java))
        }
    }

    private fun prepare(view: View) {
        setHasOptionsMenu(true)
        mContext = view.context

        (mContext as AppCompatActivity).setSupportActionBar(toolbar)
        (mContext as AppCompatActivity).supportActionBar?.title = "Daftar Kelas"

        rv_class?.layoutManager = LinearLayoutManager(view.context)
        rv_class?.setHasFixedSize(true)

        setupDatabase()
        getDataCount()

        swipe_refresh?.setOnRefreshListener {
            setupDatabase()
            getDataCount()
        }
    }

    private fun setupDatabase() {
        mUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        val query = FirebaseFirestore.getInstance()
            .collection("teacher")
            .document(mUserId.toString())
            .collection("classList")
            .orderBy("date")

        val options = FirestoreRecyclerOptions.Builder<ClassResponse>()
            .setQuery(query, ClassResponse::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = ClassListAdapter(options, this)
        rv_class?.adapter = adapter
    }

    private fun getDataCount() {
        swipe_refresh?.isRefreshing = true
        val db = FirebaseFirestore.getInstance()
            .collection("teacher")
            .document(mUserId.toString())
            .collection("classList")

        db.addSnapshotListener { snapshot, _ ->
            if ((snapshot?.size() ?: 0) > 0) {
                rv_class?.visibility = View.VISIBLE
                tv_no_data?.visibility = View.GONE
            } else {
                rv_class?.visibility = View.GONE
                tv_no_data?.visibility = View.VISIBLE
            }
            swipe_refresh?.isRefreshing = false
        }
    }

    override fun onDeleteClass(lessonId: String, classId: String) {
        swipe_refresh.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("teacher")
            .document(mUserId.toString())
            .collection("classList")
            .document(classId)
            .delete()
            .addOnSuccessListener {
                val db2 = FirebaseFirestore.getInstance()
                db2.collection("lessons")
                    .document(lessonId)
                    .collection("category")
                    .document(classId)
                    .get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            deleteClassIfPublished(lessonId, classId)
                        } else {
                            swipe_refresh.isRefreshing = false
                            Toast.makeText(context, "Kelas berhasil dihapus", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener {
                        swipe_refresh.isRefreshing = false
                        Toast.makeText(context, it.localizedMessage?.toString(), Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener {
                swipe_refresh.isRefreshing = false
                Toast.makeText(context, it.localizedMessage?.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteClassIfPublished(lessonId: String, classId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("lessons")
            .document(lessonId)
            .collection("category")
            .document(classId)
            .delete()
            .addOnSuccessListener {
                swipe_refresh.isRefreshing = false
                Toast.makeText(context, "Kelas berhasil dihapus", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                swipe_refresh.isRefreshing = false
                Toast.makeText(context, it.localizedMessage?.toString(), Toast.LENGTH_SHORT).show()
            }
    }
}