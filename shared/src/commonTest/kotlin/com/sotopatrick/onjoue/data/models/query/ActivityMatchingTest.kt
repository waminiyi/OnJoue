package com.sotopatrick.onjoue.data.models.query

import com.sotopatrick.onjoue.data.models.activity.Activity
import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.models.activity.AgeMonths
import com.sotopatrick.onjoue.data.models.activity.AgeRange
import com.sotopatrick.onjoue.data.models.activity.DurationMinutes
import com.sotopatrick.onjoue.data.models.catalogs.TagCatalog
import com.sotopatrick.onjoue.data.models.equipment.Equipment
import com.sotopatrick.onjoue.data.models.equipment.EquipmentId
import com.sotopatrick.onjoue.data.models.query.ActivityFilter
import com.sotopatrick.onjoue.data.models.query.matches
import com.sotopatrick.onjoue.data.models.tag.Tag
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.data.models.tag.TagId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ActivityMatchingTest {
    private val inside = Tag(TagId("dedans"), TagGroup.LOCATION, "Dedans")
    private val livingRoom = Tag(TagId("salon"), TagGroup.LOCATION, "Salon", parent = inside.id)
    private val outside = Tag(TagId("dehors"), TagGroup.LOCATION, "Dehors")
    private val calm = Tag(TagId("calme"), TagGroup.MOOD, "Se calmer")
    private val active = Tag(TagId("actif"), TagGroup.MOOD, "Se dépenser")
    private val catalog = TagCatalog(listOf(inside, livingRoom, outside, calm, active))
    private val ball = Equipment(EquipmentId("ballon"), "Ballon")
    private val activity = Activity(
        id = ActivityId("jeu"),
        name = "Jeu de ballon",
        description = "Faire rouler le ballon.",
        age = AgeRange(AgeMonths(24), AgeMonths(60)),
        duration = DurationMinutes(10),
        equipment = setOf(ball),
        tags = setOf(livingRoom, calm),
    )
    private val filter = ActivityFilter(childAge = AgeMonths(36))

    @Test
    fun invalidValuesAreRejected() {
        assertFailsWith<IllegalArgumentException> { ActivityId(" ") }
        assertFailsWith<IllegalArgumentException> { TagId("") }
        assertFailsWith<IllegalArgumentException> { EquipmentId("") }
        assertFailsWith<IllegalArgumentException> { AgeMonths(-1) }
        assertFailsWith<IllegalArgumentException> { DurationMinutes(0) }
        assertFailsWith<IllegalArgumentException> { DurationMinutes(-1) }
        assertFailsWith<IllegalArgumentException> { AgeRange(AgeMonths(60), AgeMonths(24)) }
    }

    @Test
    fun ageBoundsAreInclusive() {
        assertTrue(activity.matches(filter.copy(childAge = AgeMonths(24)), catalog))
        assertTrue(activity.matches(filter.copy(childAge = AgeMonths(60)), catalog))
        assertFalse(activity.matches(filter.copy(childAge = AgeMonths(23)), catalog))
        assertFalse(activity.matches(filter.copy(childAge = AgeMonths(61)), catalog))
    }

    @Test
    fun maxDurationIsInclusiveAndOptional() {
        assertTrue(activity.matches(filter, catalog))
        assertTrue(activity.matches(filter.copy(maxDuration = DurationMinutes(10)), catalog))
        assertFalse(activity.matches(filter.copy(maxDuration = DurationMinutes(9)), catalog))
    }

    @Test
    fun nullEquipmentSkipsFilteringAndEmptySetExcludesRequiredEquipment() {
        assertTrue(activity.matches(filter, catalog))
        assertFalse(activity.matches(filter.copy(availableEquipment = emptySet()), catalog))
        assertTrue(activity.matches(filter.copy(availableEquipment = setOf(ball.id)), catalog))
        assertTrue(
            activity.copy(equipment = emptySet())
                .matches(filter.copy(availableEquipment = emptySet()), catalog)
        )
    }

    @Test
    fun catalogIncludesParentAndDirectChildren() {
        assertEquals(livingRoom, catalog[livingRoom.id])
        assertEquals(null, catalog[TagId("inconnu")])
        assertEquals(setOf(inside.id, livingRoom.id), catalog.expanded(setOf(inside.id)))
        assertEquals(setOf(livingRoom.id), catalog.expanded(setOf(livingRoom.id)))
    }

    @Test
    fun tagsUseOrWithinGroupsAndAndAcrossGroups() {
        val selection = filter.copy(
            selectedTags = mapOf(
                TagGroup.LOCATION to setOf(outside.id, inside.id),
                TagGroup.MOOD to setOf(calm.id),
            )
        )
        assertTrue(activity.matches(selection, catalog))
        assertFalse(activity.copy(tags = setOf(livingRoom, active)).matches(selection, catalog))
        assertFalse(
            activity.matches(
                filter.copy(selectedTags = mapOf(TagGroup.LOCATION to setOf(outside.id))),
                catalog,
            )
        )
    }

    @Test
    fun absentGroupOrEmptySelectionDoesNotRestrictResults() {
        val selection = filter.copy(selectedTags = mapOf(TagGroup.LOCATION to setOf(outside.id)))
        assertTrue(activity.copy(tags = setOf(calm)).matches(selection, catalog))
        assertTrue(
            activity.matches(
                filter.copy(selectedTags = mapOf(TagGroup.LOCATION to emptySet())),
                catalog,
            )
        )
    }

    @Test
    fun embeddedTagsDoNotNeedCatalogLookupAndEquipmentMatchesById() {
        val partialCatalog = TagCatalog(emptyList())
        val selection = filter.copy(selectedTags = mapOf(TagGroup.MOOD to setOf(active.id)))
        assertFalse(activity.matches(selection, partialCatalog))
        assertTrue(
            activity.copy(equipment = setOf(ball.copy(label = "Grand ballon")))
                .matches(filter.copy(availableEquipment = setOf(ball.id)), partialCatalog),
        )
    }
}
