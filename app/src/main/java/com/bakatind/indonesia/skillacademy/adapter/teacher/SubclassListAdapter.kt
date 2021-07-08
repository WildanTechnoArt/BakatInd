package com.bakatind.indonesia.skillacademy.adapter.teacher

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.activity.teacher.TeacherSubjectListActivity
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.bakatind.indonesia.skillacademy.database.SharedPrefManager
import com.bakatind.indonesia.skillacademy.model.teacher.SubClassResponse
import com.bakatind.indonesia.skillacademy.util.UtilsConstant
import com.bakatind.indonesia.skillacademy.view.SubClassListListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.subclass_item.view.*
import java.net.URLEncoder

class SubclassListAdapter(
    options: FirestoreRecyclerOptions<SubClassResponse>,
    private val listener: SubClassListListener
) :
    FirestoreRecyclerAdapter<SubClassResponse, SubclassListAdapter.ViewHolder>(options) {

    private lateinit var getContext: Context
    private var classId: String? = null
    private var lessonId: String? = null
    private var className: String? = null
    private var lessonName: String? = null
    private var subclassName: String? = null
    private var subclassKey: String? = null
    private var getStatus: Boolean? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.subclass_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: SubClassResponse) {
        getContext = holder.itemView.context
        classId = item.classid.toString()
        lessonId = item.lessonid.toString()
        className = item.classname.toString()
        lessonName = item.lessonname.toString()
        getStatus = item.approve
        subclassName = item.name.toString()
        subclassKey = snapshots.getSnapshot(position).id

        holder.apply {
            containerView.tv_subclass_name.text = "${position.plus(1)}. $subclassName"

            if (getStatus == true) {
                containerView.tv_approve?.text = "Aktif"
                containerView.tv_approve.setTextColor(
                    ContextCompat.getColor(
                        getContext,
                        R.color.colorActive
                    )
                )

                containerView.btn_menu.setOnClickListener {
                    showDropdownMenu(subclassName.toString(), true, it)
                }
                containerView.setOnClickListener {
                    val intent = Intent(getContext, TeacherSubjectListActivity::class.java)
                    intent.putExtra(UtilsConstant.LESSON_NAME, lessonName)
                    intent.putExtra(UtilsConstant.LESSON_KEY, lessonId)
                    intent.putExtra(UtilsConstant.CATEGORY_NAME, className)
                    intent.putExtra(UtilsConstant.CATEGORY_KEY, classId)
                    intent.putExtra(UtilsConstant.SUB_CATEGORY_NAME, subclassName)
                    intent.putExtra(UtilsConstant.SUB_CATEGORY_KEY, subclassKey)
                    getContext.startActivity(intent)
                }
            } else {
                containerView.tv_approve?.text = "Tidak Aktif"
                containerView.tv_approve.setTextColor(
                    ContextCompat.getColor(
                        getContext,
                        R.color.colorPrimary
                    )
                )

                containerView.btn_menu.setOnClickListener {
                    showDropdownMenu(subclassName.toString(), false, it)
                }
                containerView.setOnClickListener {
                    containerView.btn_menu.callOnClick()
                }
            }
        }
    }

    private fun showDropdownMenu(subClassName: String, approved: Boolean, view: View) {
        val popupMenu = PopupMenu(getContext, view)
        popupMenu.setOnMenuItemClickListener(object :
            android.widget.PopupMenu.OnMenuItemClickListener,
            PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(p0: MenuItem?): Boolean {
                when (p0?.itemId) {
                    R.id.menu_approve -> {
                        val builder = MaterialAlertDialogBuilder(getContext, R.style.MaterialAlertDialog_rounded)
                            .setTitle("Minta Persetujuan")
                            .setMessage("Agar kelas anda dapat dipublish silakan minta persetujuan Admin")
                            .setPositiveButton("Ok") { _, _ ->
                                chatAdmin()
                            }
                            .setNegativeButton("Lain Kali") { dialog, _ ->
                                dialog.dismiss()
                            }
                        val dialog = builder.create()
                        dialog.show()
                    }
                    R.id.menu_edit -> {
                        listener.onEditClass(subclassKey.toString(), subClassName, lessonId.toString(), subclassKey.toString())
                    }
                    R.id.menu_delete -> {
                        val builder = MaterialAlertDialogBuilder(getContext, R.style.MaterialAlertDialog_rounded)
                            .setTitle("Konfirmasi")
                            .setMessage("Anda yakin ingin menghapusnya?")
                            .setPositiveButton("Ya") { _, _ ->
                                listener.onDeleteClass(lessonId.toString(), subclassKey.toString())
                            }
                            .setNegativeButton("Tidak") { dialog, _ ->
                                dialog.dismiss()
                            }
                        val dialog = builder.create()
                        dialog.show()
                    }
                }
                return true
            }
        })
        if (approved) {
            popupMenu.inflate(R.menu.teacher_item_menu)
        } else {
            popupMenu.inflate(R.menu.teacher_approve_menu)
        }
        popupMenu.show()
    }

    private fun chatAdmin() {
        val message = """
            Hallo Admin Bakatind 
            Saya ingin meminta persetujuan kelas dengan detail:
            Nama Saya:
            Email: ${SharedPrefManager.getInstance(getContext)?.getEmailUser.toString()}
            Materi Skill: $lessonName
            Kelas: $className
            Sub Kelas: $subclassName

            Terimaksih.
        """.trimIndent()

        val db = FirebaseFirestore.getInstance()
        db.collection("admin")
            .document("nomor WA")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(
                        getContext,
                        error.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val getAdmin = value?.getString("admin")

                    val phoneNumber = getAdmin?.replace("+62", "+62 ")

                    val url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" +
                            URLEncoder.encode(message, "UTF-8")

                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    getContext.startActivity(i)
                }
            }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}