package com.sotopatrick.onjoue.data.models.tried

import kotlin.jvm.JvmInline

@JvmInline
value class TriedId(val id: String) {
    init {
        require(id.isNotBlank()) { "IdTried vide" }
    }
}
