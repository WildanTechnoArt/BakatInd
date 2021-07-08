package com.bakatind.indonesia.skillacademy.view

interface CategoryListener {
    fun onClick(key: String, name: String, teacherId: String, link: String)
    fun onSubscribe(className: String)
}