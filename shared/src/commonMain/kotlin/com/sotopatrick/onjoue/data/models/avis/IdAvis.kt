package com.sotopatrick.onjoue.data.models.avis

import kotlin.jvm.JvmInline

@JvmInline
value class IdAvis(val id: String) {
    init {
        require(id.isNotBlank()) { "IdAvis vide" }
    }
}
