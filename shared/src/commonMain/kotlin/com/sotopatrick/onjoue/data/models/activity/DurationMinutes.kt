package com.sotopatrick.onjoue.data.models.activity

import kotlin.jvm.JvmInline

@JvmInline
value class DurationMinutes(val value: Int) : Comparable<DurationMinutes> {
    init {
        require(value > 0) { "Durée invalide" }
    }

    override fun compareTo(other: DurationMinutes) = value.compareTo(other.value)
}
