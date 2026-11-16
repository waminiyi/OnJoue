package com.sotopatrick.onjoue.presentation.activity

import com.sotopatrick.onjoue.data.models.activity.*
import com.sotopatrick.onjoue.data.models.tag.*
import com.sotopatrick.onjoue.data.repositories.sampleActivity
import com.sotopatrick.onjoue.data.repositories.sampleEquipment
import kotlin.test.*

class IdeaFiltersTest {
    private val inside = Tag(TagId("inside"), TagGroup.LOCATION, "Dedans")
    private val room = Tag(TagId("room"), TagGroup.LOCATION, "Salon", parent = inside.id)
    private val outside = Tag(TagId("outside"), TagGroup.LOCATION, "Dehors")
    private val activity = sampleActivity().copy(
        name = "La ronde",
        description = "Bouger ensemble",
        age = AgeRange(AgeMonths(36), AgeMonths(60)),
        tags = setOf(room),
        equipment = setOf(sampleEquipment("Ballon")),
    )

    @Test
    fun ageRangesOverlapInclusively() {
        assertTrue(IdeaFilters(age = AgeFilter.TODDLER).matches(activity))
        assertTrue(IdeaFilters(age = AgeFilter.PRESCHOOL).matches(activity))
        assertTrue(IdeaFilters(age = AgeFilter.SCHOOL).matches(activity))
        assertFalse(IdeaFilters(age = AgeFilter.TODDLER).matches(
            activity.copy(age = AgeRange(AgeMonths(37), AgeMonths(60))),
        ))
        assertTrue(IdeaFilters().matches(activity.copy(age = AgeRange(AgeMonths(0), AgeMonths(6)))))
    }

    @Test
    fun durationThresholdIsInclusive() {
        assertTrue(IdeaFilters(shortDuration = true).matches(activity.copy(duration = DurationMinutes(15))))
        assertFalse(IdeaFilters(shortDuration = true).matches(activity.copy(duration = DurationMinutes(16))))
        assertTrue(IdeaFilters().matches(activity.copy(duration = DurationMinutes(16))))
    }

    @Test
    fun rootLocationsIncludeChildrenAndUnconstrainedActivities() {
        val filter = IdeaFilters(locationId = inside.id.value)
        assertTrue(filter.matches(activity))
        assertTrue(filter.matches(activity.copy(tags = setOf(inside))))
        assertTrue(filter.matches(activity.copy(tags = emptySet())))
        assertFalse(filter.matches(activity.copy(tags = setOf(outside))))
        assertFalse(filter.matches(activity.copy(tags = setOf(outside.copy(label = "Dedans")))))
    }

    @Test
    fun querySearchesAllExistingFieldsAndTrimsWhitespace() {
        listOf(" RONDE ", "BOUGER", "sAlOn", "BALLON", "  ").forEach {
            assertTrue(IdeaFilters(query = it).matches(activity), it)
        }
        assertFalse(IdeaFilters(query = "introuvable").matches(activity))
    }

    @Test
    fun criteriaCombineAndResetRemovesEveryConstraint() {
        val filters = IdeaFilters("ronde", AgeFilter.TODDLER, inside.id.value, true)
        assertTrue(filters.active)
        assertTrue(filters.matches(activity))
        assertFalse(filters.copy(locationId = outside.id.value).matches(activity))
        assertFalse(filters.copy(query = "absent").matches(activity))
        assertFalse(filters.matches(activity.copy(duration = DurationMinutes(16))))
        assertFalse(IdeaFilters().active)
        assertFalse(IdeaFilters(query = "  ").active)
        assertTrue(IdeaFilters().matches(activity.copy(duration = DurationMinutes(60))))
    }
}
