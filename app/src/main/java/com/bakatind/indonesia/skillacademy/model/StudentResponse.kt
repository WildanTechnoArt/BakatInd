package com.bakatind.indonesia.skillacademy.model

data class StudentResponse (
    var username: String? = null,
    var device: Int? = null,
    var email: String? = null,
    var school: String? = null,
    var address: String? = null,
    var phone: String? = null,
    var datetime: String? = null,
    var teacher: Boolean? = null,
    var active: Boolean? = null,
    var approve: Boolean? = null,
)