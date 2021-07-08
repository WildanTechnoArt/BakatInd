package com.bakatind.indonesia.skillacademy.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bakatind.indonesia.skillacademy.GlideApp
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.model.teacher.SubClassResponse
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.subs_class_item.view.*

class FirestoreClassSubsAdapter(options: FirestoreRecyclerOptions<SubClassResponse>) :
    FirestoreRecyclerAdapter<SubClassResponse, FirestoreClassSubsAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.subs_class_item, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: SubClassResponse) {
        val classId = item.classid.toString()
        val lessonId = item.lessonid.toString()
        val subscribe = item.subscribe.toString()
        val bonus = item.bonus.toString()

        holder.apply {
            val db = FirebaseFirestore.getInstance()
            db.collection("lessons")
                .document(lessonId)
                .collection("category")
                .document(classId)
                .addSnapshotListener { snapshot, _ ->
                    val getImage = snapshot?.getString("image").toString()
                    val getClassName = snapshot?.getString("name").toString()
                    GlideApp.with(containerView.context)
                        .load(getImage)
                        .placeholder(R.drawable.ic_baseline_image_34)
                        .into(containerView.img_lesson)

                    containerView.tv_class_name.text = getClassName
                }

            val db2 = FirebaseFirestore.getInstance()
            db2.collection("lessons")
                .document(lessonId)
                .addSnapshotListener { snapshot, _ ->
                    val getLessonName = snapshot?.getString("lesson").toString()

                    containerView.tv_lesson_name.text = getLessonName
                }

            if (bonus != "null") {
                containerView.tv_subscribe.text = "Berlangganan: $subscribe (+Bonus $bonus)"
                containerView.tv_subscribe.isSelected = true
            } else {
                containerView.tv_subscribe.text = "Berlangganan: $subscribe"
            }

        }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}