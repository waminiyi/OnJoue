package com.sotopatrick.onjoue.data.models.tag

import kotlin.jvm.JvmInline

@JvmInline
value class TagId(val valeur: String) {
    init {
        require(valeur.isNotBlank()) { "TagId vide" }
    }
}
