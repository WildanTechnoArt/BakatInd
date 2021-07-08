package com.bakatind.indonesia.skillacademy.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bakatind.indonesia.skillacademy.GlideApp
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.model.TaskResponse
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.progress_task_item.view.*

class FirestoreTaskAdapter(options: FirestoreRecyclerOptions<TaskResponse>) :
    FirestoreRecyclerAdapter<TaskResponse, FirestoreTaskAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.progress_task_item, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: TaskResponse) {
        val score = item.score
        val message = item.message.toString()
        val mClassId = item.classid.toString()
        val mLessonId = item.lessonid.toString()

        holder.apply {
            val db = FirebaseFirestore.getInstance()
            db.collection("lessons")
                .document(mLessonId)
                .collection("category")
                .document(mClassId)
                .addSnapshotListener { snapshot, _ ->
                    val getImage = snapshot?.getString("image").toString()
                    val getClassName = snapshot?.getString("name").toString()
                    containerView.tv_class.text = "Kelas: $getClassName"
                    GlideApp.with(containerView.context)
                        .load(getImage)
                        .placeholder(R.drawable.profile_placeholder)
                        .into(containerView.img_lesson)
                }

            val db2 = FirebaseFirestore.getInstance()
            db2.collection("lessons")
                .document(mLessonId)
                .addSnapshotListener { snapshot, _ ->
                    val mLessonName = snapshot?.getString("lesson").toString()
                    containerView.tv_lesson.text = mLessonName
                }

            containerView.tv_score?.text = "Nilai: ${score ?: 0}"
            containerView.tv_message?.text = "Pesan: $message"
        }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}