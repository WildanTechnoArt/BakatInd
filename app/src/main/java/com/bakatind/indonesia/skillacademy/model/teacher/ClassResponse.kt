package com.bakatind.indonesia.skillacademy.model.teacher

import java.util.*

data class ClassResponse (
    var teacherid: String? = null,
    var lessonid: String? = null,
    var image: String? = null,
    var lessonname: String? = null,
    var classid: String? = null,
    var classname: String? = null,
    var subclassname: String? = null,
    var subclassId: String? = null,
    var name: String? = null,
    var date: Date? = null,
    var link: String? = null,
    var video: String? = null,
    var pdf: String? = null,
    var subscribe: Boolean? = null
)