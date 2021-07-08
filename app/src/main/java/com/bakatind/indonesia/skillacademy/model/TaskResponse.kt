package com.bakatind.indonesia.skillacademy.model

data class TaskResponse (
    var lessonid: String? = null,
    var classid: String? = null,
    var score: Int? = null,
    var message: String? = null,
    var rated: Boolean? = null,
    var userId: String? = null
)