package com.bakatind.indonesia.skillacademy.view

interface SubClassListListener {
    fun onDeleteClass(lessonId:String, classId: String)
    fun onEditClass(subClassId: String, subClassName: String, lessonId:String, classId: String)
}