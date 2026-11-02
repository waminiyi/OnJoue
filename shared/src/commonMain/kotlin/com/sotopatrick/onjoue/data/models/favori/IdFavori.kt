package com.sotopatrick.onjoue.data.models.favori

import kotlin.jvm.JvmInline

@JvmInline
value class IdFavori(val id: String) {
    init {
        require(id.isNotBlank()) { "IdFavori vide" }
    }
}
