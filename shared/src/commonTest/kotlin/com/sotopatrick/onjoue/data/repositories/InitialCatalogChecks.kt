package com.sotopatrick.onjoue.data.repositories

import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.data.repositories.activity.RoomActivityRepository
import com.sotopatrick.onjoue.data.repositories.equipment.RoomEquipmentRepository
import com.sotopatrick.onjoue.data.repositories.tag.RoomTagRepository
import com.sotopatrick.onjoue.data.sources.room.AppDatabase
import com.sotopatrick.onjoue.data.sources.room.seedInitialCatalog
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first

internal suspend fun verifyInitialCatalog(database: AppDatabase) {
    seedInitialCatalog(database)
    val tags = RoomTagRepository(database).observeTags().first()
    val equipment = RoomEquipmentRepository(database).observeEquipment().first()
    assertEquals(41, tags.size)
    assertEquals(57, equipment.size)
    assertEquals(TagGroup.entries.toSet(), tags.map { it.group }.toSet())
    assertEquals(41, tags.map { it.group to it.label.lowercase() }.toSet().size)
    assertEquals(57, equipment.map { it.label.lowercase() }.toSet().size)
    val byId = tags.associateBy { it.id }
    tags.filter { it.parent != null }.forEach { tag ->
        val parent = byId.getValue(tag.parent!!)
        assertEquals(TagGroup.LOCATION, tag.group)
        assertEquals(TagGroup.LOCATION, parent.group)
        assertEquals(null, parent.parent)
    }
    assertEquals(10, tags.count { it.parent != null })
    assertTrue(equipment.any { it.label == "Pâte à modeler" })
    assertTrue(tags.any { it.label == "Été" })
    val activities = RoomActivityRepository(database).observeActivities().first()
    assertEquals(88, activities.size)
    assertEquals(47, activities.count { it.document?.value?.contains("/0_12_mois/") == true })
    assertTrue(activities.all { it.steps.isNotEmpty() && it.document != null })
    assertTrue(RoomTagRepository(database).observeTagUsage().first().any { it.activityCount > 0 })
}
