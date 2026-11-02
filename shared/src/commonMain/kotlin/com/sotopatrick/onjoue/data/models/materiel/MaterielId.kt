package com.sotopatrick.onjoue.data.models.materiel

import kotlin.jvm.JvmInline

@JvmInline
value class MaterielId(val valeur: String) {
    init {
        require(valeur.isNotBlank()) { "MaterielId vide" }
    }
}
