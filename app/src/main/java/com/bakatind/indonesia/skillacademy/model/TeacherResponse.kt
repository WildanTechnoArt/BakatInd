package com.bakatind.indonesia.skillacademy.model

data class TeacherResponse (
    var username: String? = null,
    var email: String? = null,
    var experience: String? = null,
    var address: String? = null,
    var phone: String? = null,
    var device: Int? = null,
    var datetime: String? = null,
    var teacher: Boolean? = null,
    var active: Boolean? = null,
    var approve: Boolean? = null,
)