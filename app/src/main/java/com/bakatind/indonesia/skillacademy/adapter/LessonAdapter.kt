package com.bakatind.indonesia.skillacademy.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bakatind.indonesia.skillacademy.GlideApp
import com.bakatind.indonesia.skillacademy.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.bakatind.indonesia.skillacademy.activity.ClassActivity
import com.bakatind.indonesia.skillacademy.model.LessonResponse
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.LESSON_KEY
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.LESSON_NAME
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.lesson_item.view.*

class LessonAdapter(options: FirestoreRecyclerOptions<LessonResponse>) :
    FirestoreRecyclerAdapter<LessonResponse, LessonAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lesson_item, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: LessonResponse) {
        val getKey = snapshots.getSnapshot(position).id
        val getContext = holder.itemView.context
        val getLessonName = item.lesson.toString()
        val getLessonImg = item.image.toString()

        holder.apply {
            GlideApp.with(getContext)
                .load(getLessonImg)
                .into(containerView.img_lesson)

            containerView.tv_lesson.text = getLessonName
            containerView.tv_lesson.isSelected = true
            containerView.card_lesson.setOnClickListener {
                val intent = Intent(getContext, ClassActivity::class.java)
                intent.putExtra(LESSON_NAME, getLessonName)
                intent.putExtra(LESSON_KEY, getKey)
                (getContext as AppCompatActivity).startActivity(intent)
            }
        }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}