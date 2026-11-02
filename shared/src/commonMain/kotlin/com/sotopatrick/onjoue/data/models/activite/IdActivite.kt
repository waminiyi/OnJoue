package com.sotopatrick.onjoue.data.models.activite

import kotlin.jvm.JvmInline

@JvmInline
value class IdActivite(val id: String) {
    init {
        require(id.isNotBlank()) { "IdActivite vide" }
    }
}
