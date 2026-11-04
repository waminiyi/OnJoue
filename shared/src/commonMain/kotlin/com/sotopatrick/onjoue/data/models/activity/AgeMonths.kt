package com.sotopatrick.onjoue.data.models.activity

import kotlin.jvm.JvmInline

@JvmInline
value class AgeMonths(val value: Int) : Comparable<AgeMonths> {
    init {
        require(value >= 0) { "Âge négatif" }
    }

    override fun compareTo(other: AgeMonths) = value.compareTo(other.value)
}
