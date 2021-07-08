package com.bakatind.indonesia.skillacademy.fragment.teacher

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.adapter.teacher.SubclassListAdapter
import com.bakatind.indonesia.skillacademy.model.teacher.SubClassResponse
import com.bakatind.indonesia.skillacademy.util.UtilsConstant
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.FAB_BROADCAST
import com.bakatind.indonesia.skillacademy.view.SubClassListListener
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.add_class_dialog.view.*
import kotlinx.android.synthetic.main.fragment_tab_list.*

class SubclassListFragment : Fragment(), SubClassListListener {

    private var getLessonKey: String? = null
    private var getCategoryKey: String? = null
    private var getSubCategoryKey: String? = null
    private var teacherId: String? = null
    private lateinit var mIntent: Intent
    private lateinit var dialogView: View
    private var alertDialog: AlertDialog? = null


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
            (view.context as AppCompatActivity).getString(R.string.tv_not_teacher_subclass)

        getLessonKey = mIntent.getStringExtra(UtilsConstant.LESSON_KEY).toString()
        getCategoryKey = mIntent.getStringExtra(UtilsConstant.CATEGORY_KEY).toString()
        getSubCategoryKey = mIntent.getStringExtra(UtilsConstant.SUB_CATEGORY_KEY).toString()

        teacherId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        rv_data_list?.layoutManager = LinearLayoutManager(context)
        rv_data_list?.setHasFixedSize(true)
        rv_data_list?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    sendFabBroadcast(true)
                }

                if (!recyclerView.canScrollVertically(-1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    sendFabBroadcast(false)
                }
            }
        })
        swipe_refresh?.isEnabled = false

        setupDatabse()
        getDataCount()

        swipe_refresh?.setOnRefreshListener {
            setupDatabse()
            getDataCount()
        }
    }

    private fun sendFabBroadcast(condition: Boolean) {
        val sendBC = Intent()
        sendBC.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            .setAction(FAB_BROADCAST)
            .putExtra("endScroll", condition)
        activity?.sendBroadcast(sendBC)
    }

    private fun setupDatabse() {
        val query = FirebaseFirestore.getInstance()
            .collection("teacher")
            .document(teacherId.toString())
            .collection("classList")
            .document(getCategoryKey.toString())
            .collection("subCategory")
            .orderBy("date")

        val options = FirestoreRecyclerOptions.Builder<SubClassResponse>()
            .setQuery(query, SubClassResponse::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = SubclassListAdapter(options, this)
        rv_data_list?.adapter = adapter
    }

    private fun getDataCount() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
            .collection("teacher")
            .document(teacherId.toString())
            .collection("classList")
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

    override fun onDeleteClass(lessonId: String, classId: String) {
        swipe_refresh.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("teacher")
            .document(teacherId.toString())
            .collection("classList")
            .document(getCategoryKey.toString())
            .collection("subCategory")
            .document(classId)
            .delete()
            .addOnSuccessListener {
                deleteClassInPublic(lessonId)
            }.addOnFailureListener {
                swipe_refresh.isRefreshing = false
                Toast.makeText(context, it.localizedMessage?.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteClassInPublic(classId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("subCategory")
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

    @SuppressLint("InflateParams")
    override fun onEditClass(
        subClassId: String,
        subClassName: String,
        lessonId: String,
        classId: String
    ) {
        val builder =
            context?.let { MaterialAlertDialogBuilder(it, R.style.MaterialAlertDialog_rounded) }
        dialogView = (context as AppCompatActivity).layoutInflater.inflate(
            R.layout.add_class_dialog,
            null
        )

        dialogView.input_class_name.setText(subClassName)
        dialogView.btn_accept.text = context?.getString(R.string.btn_edit)

        bindProgressButton(dialogView.btn_accept)
        dialogView.btn_accept.attachTextChangeAnimator()

        dialogView.btn_accept.setOnClickListener {
            val subclassName = dialogView.input_class_name.text.toString()
            if (subclassName.isEmpty()) {
                Toast.makeText(context, "Tidak boleh kosong", Toast.LENGTH_SHORT).show()
            } else {
                dialogView.btn_accept.showProgress { progressColor = Color.WHITE }

                val db = FirebaseFirestore.getInstance()
                db.collection("teacher")
                    .document(teacherId.toString())
                    .collection("classList")
                    .document(getCategoryKey.toString())
                    .collection("subCategory")
                    .document(subClassId)
                    .update("name", subclassName)
                    .addOnSuccessListener {
                        val db2 = FirebaseFirestore.getInstance()
                        db2.collection("lessons")
                            .document(getLessonKey.toString())
                            .collection("category")
                            .document(getCategoryKey.toString())
                            .collection("subCategory")
                            .document(subClassId)
                            .get()
                            .addOnSuccessListener {
                                if (it.exists()) {
                                    editSubclassForPublic(subClassId, subclassName)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Sub Kelas berhasil diubah",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    dialogView.btn_accept.hideProgress(R.string.btn_edit)
                                    alertDialog?.dismiss()
                                }
                            }
                            .addOnFailureListener {
                                dialogView.btn_accept.hideProgress(R.string.btn_edit)
                                Toast.makeText(
                                    context,
                                    it.localizedMessage?.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    .addOnFailureListener {
                        dialogView.btn_accept.hideProgress(R.string.btn_edit)
                        Toast.makeText(context, it.localizedMessage?.toString(), Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }

        dialogView.btn_cancel.setOnClickListener {
            alertDialog?.dismiss()
        }

        builder?.setView(dialogView)
        builder?.setTitle("Edit Sub Kelas Baru")

        alertDialog = builder?.create()
        alertDialog?.show()
    }

    private fun editSubclassForPublic(subClassId: String, subclassName: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("lessons")
            .document(getLessonKey.toString())
            .collection("category")
            .document(getCategoryKey.toString())
            .collection("subCategory")
            .document(subClassId)
            .update("name", subclassName)
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    "Sub Kelas berhasil diubah",
                    Toast.LENGTH_SHORT
                ).show()
                dialogView.btn_accept.hideProgress(R.string.btn_edit)
                alertDialog?.dismiss()
            }
            .addOnFailureListener {
                dialogView.btn_accept.hideProgress(R.string.btn_edit)
                Toast.makeText(
                    context,
                    it.localizedMessage?.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}