package com.bakatind.indonesia.skillacademy.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bakatind.indonesia.skillacademy.GlideApp
import com.bakatind.indonesia.skillacademy.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bakatind.indonesia.skillacademy.model.CategoryResponse
import com.bakatind.indonesia.skillacademy.view.CategoryListener
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.lesson_item.view.*

class ClassAdapter(
    options: FirestoreRecyclerOptions<CategoryResponse>,
    private var listener: CategoryListener
) :
    FirestoreRecyclerAdapter<CategoryResponse, ClassAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lesson_item, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: CategoryResponse) {
        val getKey = snapshots.getSnapshot(position).id
        val getContext = holder.itemView.context
        val getCategoryName = item.name.toString()
        val getLessonImg = item.image.toString()
        val getLink = item.link.toString()
        val getIsSubscribe = item.subscribe
        val db = FirebaseFirestore.getInstance()
        val mUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val getTeacherId = item.teacherid.toString()

        holder.apply {

            db.collection("subscribe")
                .document(mUserId)
                .collection("class")
                .document(getKey)
                .get()
                .addOnSuccessListener {
                    GlideApp.with(getContext)
                        .load(getLessonImg)
                        .into(containerView.img_lesson)

                    if (getIsSubscribe == true && it.exists() || (mUserId == getTeacherId)) {
                        // Is Unlocked
                        containerView.img_lock.visibility = GONE
                        containerView.img_lesson.visibility = VISIBLE
                        containerView.card_lesson.setOnClickListener {
                            listener.onClick(getKey, getCategoryName, getTeacherId, getLink)
                        }
                    } else {
                        // Is Locked
                        containerView.img_lock.visibility = VISIBLE
                        containerView.img_lesson.visibility = GONE
                        containerView.card_lesson.setOnClickListener {
                            listener.onSubscribe(getCategoryName)
                        }
                    }

                    containerView.tv_lesson.text = getCategoryName
                    containerView.tv_lesson.isSelected = true
                }.addOnFailureListener {
                    Toast.makeText(getContext, it.localizedMessage?.toString(), Toast.LENGTH_SHORT).show()
                }
        }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}