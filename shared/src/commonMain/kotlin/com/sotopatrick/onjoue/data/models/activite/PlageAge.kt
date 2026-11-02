package com.sotopatrick.onjoue.data.models.activite

data class PlageAge(val min: AgeMois, val max: AgeMois) {
    init {
        require(min <= max) { "Âge min > âge max" }
    }

    operator fun contains(age: AgeMois) = age in min..max
}
