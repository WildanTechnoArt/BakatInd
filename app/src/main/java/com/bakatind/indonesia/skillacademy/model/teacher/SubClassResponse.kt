package com.bakatind.indonesia.skillacademy.model.teacher

import java.util.*

data class SubClassResponse (
    var teacherid: String? = null,
    var lessonid: String? = null,
    var image: String? = null,
    var lessonname: String? = null,
    var classid: String? = null,
    var classname: String? = null,
    var name: String? = null,
    var subclassId: String? = null,
    var subjectname: String? = null,
    var date: Date? = null,
    var link: String? = null,
    var video: String? = null,
    var approve: Boolean? = null,
    var pdf: String? = null,
    var subscribe: String? = null,
    var bonus: String? = null
)