package com.bakatind.indonesia.skillacademy.model

data class CategoryResponse(
    var name: String? = null,
    var number: Long? = null,
    var pdf: String? = null,
    var video: String? = null,
    var audio: String? = null,
    var image: String? = null,
    var link: String? = null,
    var subscribe: Boolean? = null,
    var teacherid: String? = null
)