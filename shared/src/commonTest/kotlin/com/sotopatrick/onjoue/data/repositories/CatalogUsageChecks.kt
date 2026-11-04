package com.sotopatrick.onjoue.data.repositories

import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.data.repositories.activity.RoomActivityRepository
import com.sotopatrick.onjoue.data.repositories.equipment.RoomEquipmentRepository
import com.sotopatrick.onjoue.data.repositories.tag.RoomTagRepository
import com.sotopatrick.onjoue.data.sources.room.AppDatabase
import kotlin.test.assertEquals
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withTimeout

internal suspend fun verifyCatalogUsage(database: AppDatabase) {
    val tags = RoomTagRepository(database, true)
    val equipment = RoomEquipmentRepository(database, true)
    val activities = RoomActivityRepository(database, true)
    val inside = sampleTag("Dedans", TagGroup.LOCATION).also { tags.save(it) }
    val outside = sampleTag("Dehors", TagGroup.LOCATION).also { tags.save(it) }
    val living = sampleTag("Salon", TagGroup.LOCATION, inside.id).also { tags.save(it) }
    val kitchen = sampleTag("Cuisine", TagGroup.LOCATION, inside.id).also { tags.save(it) }
    val unusedTag = sampleTag("Calme", TagGroup.MOOD).also { tags.save(it) }
    val ball = sampleEquipment("Ballon").also { equipment.save(it) }
    val chair = sampleEquipment("Chaise").also { equipment.save(it) }
    val unusedEquipment = sampleEquipment("Corde").also { equipment.save(it) }
    val first = sampleActivity().copy(
        id = ActivityId("first"), tags = setOf(inside, living, kitchen), equipment = setOf(ball, chair),
    ).also { activities.save(it) }
    val second = sampleActivity().copy(
        id = ActivityId("second"), tags = setOf(living), equipment = setOf(ball),
    ).also { activities.save(it) }
    val third = sampleActivity().copy(id = ActivityId("third"), tags = setOf(outside))
        .also { activities.save(it) }

    val initialTags = tags.observeTagUsage().first().associate { it.tag.id to it.activityCount }
    assertEquals(mapOf(inside.id to 2, outside.id to 1, living.id to 2, kitchen.id to 1, unusedTag.id to 0), initialTags)
    val initialEquipment = equipment.observeEquipmentUsage().first().associate { it.equipment.id to it.activityCount }
    assertEquals(mapOf(ball.id to 2, chair.id to 1, unusedEquipment.id to 0), initialEquipment)

    coroutineScope {
        val tagReady = CompletableDeferred<Unit>()
        val equipmentReady = CompletableDeferred<Unit>()
        val updatedTags = async(Dispatchers.Default) {
            withTimeout(10_000) {
                tags.observeTagUsage().onEach { tagReady.complete(Unit) }.first { rows ->
                    rows.single { it.tag.id == inside.id }.activityCount == 1
                }
            }
        }
        val updatedEquipment = async(Dispatchers.Default) {
            withTimeout(10_000) {
                equipment.observeEquipmentUsage().onEach { equipmentReady.complete(Unit) }.first { rows ->
                    rows.single { it.equipment.id == ball.id }.activityCount == 1
                }
            }
        }
        tagReady.await()
        equipmentReady.await()
        activities.save(second.copy(tags = emptySet(), equipment = emptySet()))
        assertEquals(1, updatedTags.await().single { it.tag.id == living.id }.activityCount)
        assertEquals(1, updatedEquipment.await().single { it.equipment.id == chair.id }.activityCount)
    }

    tags.save(living.copy(parent = outside.id))
    val reparented = tags.observeTagUsage().first().associate { it.tag.id to it.activityCount }
    assertEquals(1, reparented.getValue(inside.id))
    assertEquals(2, reparented.getValue(outside.id))
    activities.deleteActivity(first.id)
    val afterDelete = tags.observeTagUsage().first().associate { it.tag.id to it.activityCount }
    assertEquals(0, afterDelete.getValue(inside.id))
    assertEquals(1, afterDelete.getValue(outside.id))
    assertEquals(0, afterDelete.getValue(living.id))
    assertEquals(0, afterDelete.getValue(kitchen.id))
    assertEquals(setOf(0), equipment.observeEquipmentUsage().first().map { it.activityCount }.toSet())
    activities.deleteActivity(third.id)
    assertEquals(setOf(0), tags.observeTagUsage().first().map { it.activityCount }.toSet())
}
