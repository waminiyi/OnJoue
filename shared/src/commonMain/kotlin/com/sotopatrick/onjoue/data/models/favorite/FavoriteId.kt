package com.sotopatrick.onjoue.data.models.favorite

import kotlin.jvm.JvmInline

@JvmInline
value class FavoriteId(val id: String) {
    init {
        require(id.isNotBlank()) { "IdFavori vide" }
    }
}
