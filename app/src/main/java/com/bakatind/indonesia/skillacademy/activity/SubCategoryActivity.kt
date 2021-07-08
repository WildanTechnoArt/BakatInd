package com.bakatind.indonesia.skillacademy.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View.GONE
import androidx.appcompat.app.AppCompatActivity
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.activity.student.ReviewActivity
import com.bakatind.indonesia.skillacademy.adapter.PagerAdapterSubclass
import com.bakatind.indonesia.skillacademy.database.SharedPrefManager
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.CATEGORY_KEY
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.CATEGORY_NAME
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.LESSON_KEY
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.LESSON_NAME
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.LINK_TASK
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_subclass.*
import kotlinx.android.synthetic.main.toolbar_layout.toolbar

class SubCategoryActivity : AppCompatActivity() {

    private var getLesson: String? = null
    private var getLessonKey: String? = null
    private var getCategory: String? = null
    private var getCategoryKey: String? = null
    private var getLink: String? = null
    private var isTeacher: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subclass)
        prepare()
    }

    private fun prepare() {
        getLesson = intent.getStringExtra(LESSON_NAME).toString()
        getLessonKey = intent.getStringExtra(LESSON_KEY).toString()
        getCategory = intent.getStringExtra(CATEGORY_NAME).toString()
        getCategoryKey = intent.getStringExtra(CATEGORY_KEY).toString()
        getLink = intent.getStringExtra(LINK_TASK).toString()

        isTeacher = SharedPrefManager.getInstance(this)?.getIsTeacher

        setSupportActionBar(toolbar)
        supportActionBar?.title = getCategory

        val tabMenus = arrayOf(
            getString(R.string.class_list),
            getString(R.string.student_list),
            getString(R.string.review)
        )

        val pageAdapter = PagerAdapterSubclass(this)

        viewpager.adapter = pageAdapter

        TabLayoutMediator(
            tabs,
            viewpager
        ) { tab, position ->
            tab.text = tabMenus[position]
        }.attach()

        if (isTeacher == true) {
            fab_review.visibility = GONE
        }

        fab_review.setOnClickListener {
            val intent = Intent(this, ReviewActivity::class.java)
            intent.putExtra(CATEGORY_NAME, getCategory)
            intent.putExtra(LESSON_KEY, getLessonKey)
            intent.putExtra(CATEGORY_KEY, getCategoryKey)
            startActivity(intent)
        }

        fab_send_task.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getLink)
            )
            val chooseIntent = Intent.createChooser(intent, "Choose from below")
            startActivity(chooseIntent)
        }
    }
}