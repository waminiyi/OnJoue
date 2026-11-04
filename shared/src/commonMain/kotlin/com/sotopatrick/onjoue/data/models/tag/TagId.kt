package com.sotopatrick.onjoue.data.models.tag

import kotlin.jvm.JvmInline

@JvmInline
value class TagId(val value: String) {
    init {
        require(value.isNotBlank()) { "TagId vide" }
    }
}
