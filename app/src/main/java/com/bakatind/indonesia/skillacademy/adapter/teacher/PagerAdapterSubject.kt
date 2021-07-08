package com.bakatind.indonesia.skillacademy.adapter.teacher

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bakatind.indonesia.skillacademy.fragment.teacher.ReviewListFragment
import com.bakatind.indonesia.skillacademy.fragment.teacher.StudentListFragment
import com.bakatind.indonesia.skillacademy.fragment.teacher.SubclassListFragment

class PagerAdapterSubject(fm: FragmentActivity) :
    FragmentStateAdapter(fm) {

    private val pages =
        listOf(SubclassListFragment(), StudentListFragment(), ReviewListFragment())

    override fun getItemCount(): Int {
        return pages.size
    }

    override fun createFragment(position: Int): Fragment {
        return pages[position]
    }
}