package com.sotopatrick.onjoue.data.models.activite

import kotlin.jvm.JvmInline

@JvmInline
value class AgeMois(val valeur: Int) : Comparable<AgeMois> {
    init {
        require(valeur >= 0) { "Âge négatif" }
    }

    override fun compareTo(other: AgeMois) = valeur.compareTo(other.valeur)
}
