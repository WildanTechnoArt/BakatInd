package com.bakatind.indonesia.skillacademy.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bakatind.indonesia.skillacademy.GlideApp
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.activity.student.StudentProfileActivity
import com.bakatind.indonesia.skillacademy.activity.teacher.EditScoreActivity
import com.bakatind.indonesia.skillacademy.activity.teacher.GiveScoreActivity
import com.bakatind.indonesia.skillacademy.model.StudentModel
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.CATEGORY_KEY
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.EMAIL
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.GET_PROFILE
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.IMAGE
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.LESSON_KEY
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.MESSAGE
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.SCORE
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.USERNAME
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.student_item.view.*

class StudentListAdapter(
    options: FirestoreRecyclerOptions<StudentModel>,
    private val teacherIdInClass: String
) :
    FirestoreRecyclerAdapter<StudentModel, StudentListAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.student_item, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: StudentModel) {
        val getUserId = snapshots.getSnapshot(position).id
        val context = holder.containerView.context
        val userId: String = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val mClassId = item.classid.toString()
        val mLessonId = item.lessonid.toString()
        var getPhoto: String? = null
        var username: String? = null
        var email: String? = null

        holder.apply {
            val db = FirebaseFirestore.getInstance()
            db.collection("photos")
                .document(getUserId)
                .addSnapshotListener { snapshot, _ ->
                    getPhoto = snapshot?.getString("photoUrl").toString()
                    GlideApp.with(containerView.context)
                        .load(getPhoto)
                        .placeholder(R.drawable.profile_placeholder)
                        .into(containerView.img_profile)
                }

            val db1 = FirebaseFirestore.getInstance()
            db1.collection("users")
                .document(getUserId)
                .addSnapshotListener { snapshot, _ ->
                    username = snapshot?.getString("username").toString()
                    containerView.tv_teacher_name.text = username
                }

            val db2 = FirebaseFirestore.getInstance()
            db2.collection("users")
                .document(getUserId)
                .addSnapshotListener { snapshot, _ ->
                    email = snapshot?.getString("email").toString()
                    containerView.tv_class.text =
                        String.format(context?.getString(R.string.show_email).toString(), email)
                }

            val db3 = FirebaseFirestore.getInstance()
            db3.collection("subscribe")
                .document(getUserId)
                .collection("class")
                .document(mClassId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot?.exists() == true) {
                        containerView.img_home.visibility = VISIBLE
                    } else {
                        containerView.img_home.visibility = GONE
                    }
                }

            val db4 = FirebaseFirestore.getInstance()
            db4.collection("student")
                .document(getUserId)
                .collection("score")
                .document(mClassId)
                .addSnapshotListener { snapshot, _ ->
                    val status = snapshot?.getBoolean("rated")
                    val score = snapshot?.getLong("score")
                    val message = snapshot?.getString("message")
                    containerView.tv_score.text = "Nilai: ${score ?: 0}"

                    if (status == true) {
                        containerView.btn_score.text = context.getString(R.string.btn_edit_score)
                        val intent = Intent(context, EditScoreActivity::class.java)
                        containerView.btn_score.setOnClickListener {
                            intent.putExtra(IMAGE, getPhoto)
                            intent.putExtra(USERNAME, username)
                            intent.putExtra(EMAIL, email)
                            intent.putExtra(LESSON_KEY, mLessonId)
                            intent.putExtra(CATEGORY_KEY, mClassId)
                            intent.putExtra(GET_PROFILE, getUserId)
                            intent.putExtra(MESSAGE, message)
                            intent.putExtra(SCORE, score)
                            context.startActivity(intent)
                        }
                    } else {
                        containerView.btn_score.setOnClickListener {
                            val intent = Intent(context, GiveScoreActivity::class.java)
                            intent.putExtra(IMAGE, getPhoto)
                            intent.putExtra(USERNAME, username)
                            intent.putExtra(EMAIL, email)
                            intent.putExtra(LESSON_KEY, mLessonId)
                            intent.putExtra(CATEGORY_KEY, mClassId)
                            intent.putExtra(GET_PROFILE, getUserId)
                            context.startActivity(intent)
                        }
                    }
                }

            if (userId != getUserId) {
                containerView.tv_score.visibility = GONE
            }

            if (userId == teacherIdInClass) {
                containerView.btn_score.visibility = VISIBLE
                containerView.tv_score.visibility = VISIBLE
            } else {
                containerView.btn_score.visibility = GONE
            }

            containerView.card_teacher.setOnClickListener {
                toProfileActivity(position, context)
            }
        }
    }

    private fun toProfileActivity(position: Int, context: Context) {
        val getUserId = snapshots.getSnapshot(position).id
        val intent = Intent(context, StudentProfileActivity::class.java)
        intent.putExtra(GET_PROFILE, getUserId)
        (context as AppCompatActivity).startActivity(intent)
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer
}