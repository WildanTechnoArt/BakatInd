package com.bakatind.indonesia.skillacademy.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bakatind.indonesia.skillacademy.fragment.teacher.ClassListFragment
import com.bakatind.indonesia.skillacademy.fragment.teacher.ReviewListFragment
import com.bakatind.indonesia.skillacademy.fragment.teacher.StudentListFragment

class PagerAdapterSubclass(fm: FragmentActivity) :
    FragmentStateAdapter(fm) {

    private val pages =
        listOf(ClassListFragment(), StudentListFragment(), ReviewListFragment())

    override fun getItemCount(): Int {
        return pages.size
    }

    override fun createFragment(position: Int): Fragment {
        return pages[position]
    }
}