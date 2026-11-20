package com.sotopatrick.onjoue.presentation.activity.listing.components

import com.sotopatrick.onjoue.data.models.activity.Activity
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.data.models.tag.TagId

internal enum class AgeFilter(val label: String, val minMonths: Int, val maxMonths: Int) {
    ALL("Tous âges", 0, Int.MAX_VALUE),
    TODDLER("1–3 ans", 12, 36),
    PRESCHOOL("3–5 ans", 36, 60),
    SCHOOL("5–8 ans", 60, 96),
}

internal data class IdeaFilters(
    val age: AgeFilter = AgeFilter.ALL,
    val locationId: String? = null,
    val shortDuration: Boolean = false,
) {
    val active: Boolean get() = age != AgeFilter.ALL || locationId != null || shortDuration

    fun matches(activity: Activity): Boolean {
        val matchesAge = activity.age.min.value <= age.maxMonths && activity.age.max.value >= age.minMonths
        val places = activity.tags.filter { it.group == TagGroup.LOCATION }
        val selected = locationId?.let(::TagId)
        val matchesLocation = selected == null || places.isEmpty() ||
            places.any { it.id == selected || it.parent == selected }
        return matchesAge && matchesLocation &&
            (!shortDuration || activity.duration.value <= QuickDurationMinutes)
    }
}

private const val QuickDurationMinutes = 15
