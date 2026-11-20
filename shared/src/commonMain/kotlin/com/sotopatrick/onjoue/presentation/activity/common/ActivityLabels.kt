package com.sotopatrick.onjoue.presentation.activity.common

import com.sotopatrick.onjoue.data.models.activity.Activity
import com.sotopatrick.onjoue.data.models.activity.AgeMonths

private const val MonthsPerYear = 12

internal fun ageRangeLabel(activity: Activity): String {
    val min = activity.age.min.value
    val max = activity.age.max.value
    return if (min % MonthsPerYear == 0 && max % MonthsPerYear == 0) {
        "${min / MonthsPerYear}–${max / MonthsPerYear} ans"
    } else {
        "${ageLabel(activity.age.min)}–${ageLabel(activity.age.max)}"
    }
}

private fun ageLabel(age: AgeMonths): String {
    val years = age.value / MonthsPerYear
    val months = age.value % MonthsPerYear
    val yearLabel = if (years == 1) "1 an" else "$years ans"
    return when {
        years == 0 -> "$months mois"
        months == 0 -> yearLabel
        else -> "$yearLabel $months mois"
    }
}
