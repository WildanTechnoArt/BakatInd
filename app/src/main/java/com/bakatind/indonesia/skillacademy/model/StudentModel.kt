package com.bakatind.indonesia.skillacademy.model

data class StudentModel (
    var username: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var school: String? = null,
    var address: String? = null,
    var teacher: Boolean? = null,
    var approve: Boolean? = null,
    var datetime: String? = null,
    var classid: String? = null,
    var lessonid: String? = null,
    var active: Boolean? = null
)