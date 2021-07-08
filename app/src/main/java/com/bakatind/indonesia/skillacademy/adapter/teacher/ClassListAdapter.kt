package com.bakatind.indonesia.skillacademy.adapter.teacher

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bakatind.indonesia.skillacademy.GlideApp
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.activity.teacher.EditClassActivity
import com.bakatind.indonesia.skillacademy.activity.teacher.TeacherSubclassListActivity
import com.bakatind.indonesia.skillacademy.model.teacher.ClassResponse
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.CATEGORY_KEY
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.CATEGORY_NAME
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.CLASS_IMAGE
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.LESSON_KEY
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.LESSON_NAME
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.TEACHER_ID
import com.bakatind.indonesia.skillacademy.view.ClassListListener
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.profile_classlist_item.view.*

class ClassListAdapter(
    options: FirestoreRecyclerOptions<ClassResponse>,
    private val listener: ClassListListener
) :
    FirestoreRecyclerAdapter<ClassResponse, ClassListAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_classlist_item, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: ClassResponse) {
        val classId = item.classid
        val lessonId = item.lessonid.toString()
        val getImage = item.image.toString()
        val mClassName = item.classname.toString()
        val mTeacherId = item.teacherid.toString()
        val mContext = holder.itemView.context
        var mLessonName: String? = null
        var totalStudent: Int?

        holder.apply {
            GlideApp.with(containerView.context)
                .load(getImage)
                .placeholder(R.drawable.ic_image_200)
                .into(containerView.img_lesson)

            containerView.tv_class_name.text = mClassName

            val db = FirebaseFirestore.getInstance()
            db.collection("lessons")
                .document(lessonId)
                .collection("category")
                .document(classId.toString())
                .collection("studentList")
                .addSnapshotListener { value, _ ->
                    totalStudent = value?.size()

                    containerView.tv_total_students.text =
                        "Jumlah Pelajar: ${totalStudent ?: 0} Orang"
                }

            val db2 = FirebaseFirestore.getInstance()
            db2.collection("lessons")
                .document(lessonId)
                .addSnapshotListener { snapshot, _ ->
                    mLessonName = snapshot?.getString("lesson").toString()
                    containerView.tv_lesson_name.text = mLessonName
                }

            containerView.btn_menu.setOnClickListener {
                val popupMenu = PopupMenu(mContext, it)
                popupMenu.setOnMenuItemClickListener(object :
                    android.widget.PopupMenu.OnMenuItemClickListener,
                    PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(p0: MenuItem?): Boolean {
                        when (p0?.itemId) {
                            R.id.menu_edit -> {
                                val intent = Intent(mContext, EditClassActivity::class.java)
                                intent.putExtra(LESSON_NAME, mLessonName)
                                intent.putExtra(LESSON_KEY, lessonId)
                                intent.putExtra(CATEGORY_KEY, classId)
                                intent.putExtra(CATEGORY_NAME, mClassName)
                                intent.putExtra(CLASS_IMAGE, getImage)
                                mContext.startActivity(intent)
                            }

                            R.id.menu_delete -> {
                                val builder = MaterialAlertDialogBuilder(mContext, R.style.MaterialAlertDialog_rounded)
                                    .setTitle("Konfirmasi")
                                    .setMessage("Anda yakin ingin menghapusnya?")
                                    .setPositiveButton("Ya") { _, _ ->
                                        listener.onDeleteClass(
                                            lessonId,
                                            classId.toString()
                                        )
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

                popupMenu.inflate(R.menu.class_item_menu)
                popupMenu.show()
            }

            containerView.setOnClickListener {
                val intent = Intent(mContext, TeacherSubclassListActivity::class.java)
                intent.putExtra(LESSON_NAME, mLessonName)
                intent.putExtra(LESSON_KEY, lessonId)
                intent.putExtra(CATEGORY_NAME, mClassName)
                intent.putExtra(CATEGORY_KEY, classId)
                intent.putExtra(TEACHER_ID, mTeacherId)
                mContext.startActivity(intent)
            }
        }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}