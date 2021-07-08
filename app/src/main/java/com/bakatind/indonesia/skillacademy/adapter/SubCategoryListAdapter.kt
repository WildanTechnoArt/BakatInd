package com.bakatind.indonesia.skillacademy.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bakatind.indonesia.skillacademy.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.bakatind.indonesia.skillacademy.activity.LinkActivity
import com.bakatind.indonesia.skillacademy.activity.PdfViewActivity
import com.bakatind.indonesia.skillacademy.activity.VideoPlayerActivity
import com.bakatind.indonesia.skillacademy.model.CategoryResponse
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.LINK_LESSON
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.PDF_URL
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.VIDEO_URL
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.category_item.view.card_category
import kotlinx.android.synthetic.main.category_item.view.tv_category
import kotlinx.android.synthetic.main.sub_category_item.view.*

class SubCategoryListAdapter(
    options: FirestoreRecyclerOptions<CategoryResponse>
) :
    FirestoreRecyclerAdapter<CategoryResponse, SubCategoryListAdapter.ViewHolder>(options) {

    private lateinit var getContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sub_category_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: CategoryResponse) {
        getContext = holder.itemView.context
        val getSubCategoryName = item.name.toString()
        val getFilePdf = item.pdf.toString()
        val getFileVideo = item.video.toString()
        val getFileAudio = item.audio.toString()
        val getLink = item.link.toString()

        holder.apply {
            if (getFilePdf != "null") {
                containerView.tv_book.visibility = View.VISIBLE
                containerView.tv_book.isSelected = true
            } else {
                containerView.tv_book.visibility = View.GONE
            }

            if (getFileVideo != "null") {
                containerView.tv_video.visibility = View.VISIBLE
                containerView.tv_video.isSelected = true
            } else {
                containerView.tv_video.visibility = View.GONE
            }

            if (getFileAudio != "null") {
                containerView.tv_audio.visibility = View.VISIBLE
                containerView.tv_audio.isSelected = true
            } else {
                containerView.tv_audio.visibility = View.GONE
            }

            if (getLink != "null") {
                containerView.tv_link.visibility = View.VISIBLE
                containerView.tv_link.isSelected = true
            } else {
                containerView.tv_link.visibility = View.GONE
            }

            if ((getFilePdf != "null") || (getFileVideo != "null") || (getFileAudio != "null") || (getLink != "null")) {
                containerView.tv_media.visibility = View.GONE
            } else {
                containerView.tv_media.visibility = View.VISIBLE
            }

            containerView.tv_category.text = "${position.plus(1)}. $getSubCategoryName"
            containerView.card_category.setOnClickListener {
                showAlertDialog(getFilePdf, getLink, getFileVideo)
            }
        }
    }

    private fun showAlertDialog(getFilePdf: String, getLink: String, video: String) {
        val items: Array<CharSequence> = arrayOf("Tulisan", "Video", "Audio", "Akses Link")

        val alert = MaterialAlertDialogBuilder(getContext, R.style.MaterialAlertDialog_rounded)
            .setTitle("Pilih Media Pembelajaran")
            .setItems(items) { _, position ->
                when (position) {
                    0 -> {
                        if (getFilePdf != "null") {
                            val intent = Intent(getContext, PdfViewActivity::class.java)
                            intent.putExtra(PDF_URL, getFilePdf)
                            (getContext as AppCompatActivity).startActivity(intent)
                        } else {
                            Toast.makeText(
                                getContext,
                                getContext.getString(R.string.unable_to_access_note),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    1 -> {
                        if (video != "null") {
                            val i = Intent(getContext, VideoPlayerActivity::class.java)
                            i.putExtra(VIDEO_URL, video)
                            getContext.startActivity(i)
                        } else {
                            Toast.makeText(
                                getContext,
                                getContext.getString(R.string.unable_to_access_link),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    2 -> {
                        Toast.makeText(getContext, "Konten Belum Bisa Diakses", Toast.LENGTH_SHORT)
                            .show()
                    }
                    3 -> {
                        if (getLink != "null") {
                            val i = Intent(getContext, LinkActivity::class.java)
                            i.putExtra(LINK_LESSON, getLink)
                            getContext.startActivity(i)
                        } else {
                            Toast.makeText(
                                getContext,
                                getContext.getString(R.string.unable_to_access_link),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        alert.create()
        alert.show()
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}