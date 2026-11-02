package com.sotopatrick.onjoue.data.models.activite

import kotlin.jvm.JvmInline

@JvmInline
value class DureeMinutes(val valeur: Int) : Comparable<DureeMinutes> {
    init {
        require(valeur > 0) { "Durée invalide" }
    }

    override fun compareTo(other: DureeMinutes) = valeur.compareTo(other.valeur)
}
