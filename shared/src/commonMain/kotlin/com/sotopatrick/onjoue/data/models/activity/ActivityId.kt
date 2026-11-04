package com.sotopatrick.onjoue.data.models.activity

import kotlin.jvm.JvmInline

@JvmInline
value class ActivityId(val id: String) {
    init {
        require(id.isNotBlank()) { "IdActivite vide" }
    }
}
