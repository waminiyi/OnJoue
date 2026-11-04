package com.sotopatrick.onjoue.data.models.activity


data class AgeRange(val min: AgeMonths, val max: AgeMonths) {
    init {
        require(min <= max) { "Âge min > âge max" }
    }

    operator fun contains(age: AgeMonths) = age in min..max
}
