package com.bakatind.indonesia.skillacademy.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bakatind.indonesia.skillacademy.GlideApp
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.model.ReviewModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.review_item.view.*
import kotlinx.android.synthetic.main.review_item.view.img_profile
import kotlinx.android.synthetic.main.review_item.view.tv_name

class ReviewListAdapter(
    options: FirestoreRecyclerOptions<ReviewModel>, private val context: Context
) :
    FirestoreRecyclerAdapter<ReviewModel, ReviewListAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.review_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: ReviewModel) {
        val userId = item.userid.toString()
        val review = item.review.toString()
        val rating = item.rating

        holder.apply {
            val db = FirebaseFirestore.getInstance()
            db.collection("photos")
                .document(userId)
                .addSnapshotListener { snapshot, _ ->
                    val getPhoto = snapshot?.getString("photoUrl").toString()
                    GlideApp.with(context)
                        .load(getPhoto)
                        .placeholder(R.drawable.profile_placeholder)
                        .into(containerView.img_profile)
                }

            val db1 = FirebaseFirestore.getInstance()
            db1.collection("users")
                .document(userId)
                .addSnapshotListener { snapshot, _ ->
                    val username = snapshot?.getString("username").toString()
                    containerView.tv_name.text = username
                }

            containerView.tv_review.text = review
            containerView.ratingBar.rating = rating?.toFloat()!!
        }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}