package com.sotopatrick.onjoue.data.models.review

import kotlin.jvm.JvmInline

@JvmInline
value class ReviewId(val id: String) {
    init {
        require(id.isNotBlank()) { "IdAvis vide" }
    }
}
