package com.bakatind.indonesia.skillacademy.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bakatind.indonesia.skillacademy.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.bakatind.indonesia.skillacademy.model.CategoryResponse
import com.bakatind.indonesia.skillacademy.view.SubClassListener
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.category_item.view.*

class SubCategoryAdapter(
    options: FirestoreRecyclerOptions<CategoryResponse>,
    private var listener: SubClassListener
) :
    FirestoreRecyclerAdapter<CategoryResponse, SubCategoryAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_item, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: CategoryResponse) {
        val getKey = snapshots.getSnapshot(position).id
        val getSubCategoryName = item.name.toString()

        holder.apply {
            containerView.tv_category.text = "${position.plus(1)}. $getSubCategoryName"
            containerView.card_category.setOnClickListener {
                listener.onClick(getKey, getSubCategoryName)
            }
        }
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}