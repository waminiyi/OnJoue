package com.sotopatrick.onjoue.data.sources.catalog

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.serialization.SerializationException

class CatalogSnapshotTest {
    private val catalog = CatalogSnapshot(
        formatVersion = 1,
        revision = 1,
        tags = listOf(CatalogTag("tag", "Jeu", "TYPE")),
        equipment = listOf(CatalogEquipment("ball", "Ballon", "ball")),
        activities = listOf(
            CatalogActivity(
                id = "activity", name = "Jeu", description = "", minAgeMonths = 12, maxAgeMonths = 36,
                durationMinutes = 10, steps = listOf("Jouer"), variants = emptyList(),
                tagIds = listOf("tag"), equipmentIds = listOf("ball"),
                image = "https://firebasestorage.googleapis.com/v0/b/onjoue/o/activity.webp?alt=media",
            ),
        ),
    )

    @Test
    fun acceptsActivityWithoutIconAndKeepsEquipmentIcon() {
        validateCatalogSnapshot(catalog)
        assertEquals("ball", catalog.equipment.single().iconId)
        assertEquals(
            "https://firebasestorage.googleapis.com/v0/b/onjoue/o/activity.webp?alt=media",
            catalog.activities.single().image,
        )
    }

    @Test
    fun rejectsNonHttpsActivityImageUrls() {
        assertFailsWith<IllegalArgumentException> {
            validateCatalogSnapshot(
                catalog.copy(activities = listOf(catalog.activities.single().copy(image = "http://example.org/image"))),
            )
        }
    }

    @Test
    fun validatesFormatAndRevision() {
        assertFailsWith<IllegalArgumentException> { validateCatalogSnapshot(catalog.copy(formatVersion = 2)) }
        assertFailsWith<IllegalArgumentException> { validateCatalogSnapshot(catalog.copy(revision = 0)) }
    }

    @Test
    fun validatesNormalizedLabelsAndReferences() {
        assertFailsWith<IllegalArgumentException> {
            validateCatalogSnapshot(catalog.copy(equipment = catalog.equipment + CatalogEquipment("b", " ballon ", "b")))
        }
        assertFailsWith<IllegalArgumentException> {
            validateCatalogSnapshot(
                catalog.copy(activities = listOf(catalog.activities.single().copy(tagIds = listOf("missing")))),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            validateCatalogSnapshot(
                catalog.copy(activities = listOf(catalog.activities.single().copy(equipmentIds = listOf("ball", "ball")))),
            )
        }
    }

    @Test
    fun validatesLocationHierarchy() {
        val root = CatalogTag("root", "Maison", "LOCATION")
        val child = CatalogTag("child", "Salon", "LOCATION", "root")
        validateCatalogSnapshot(catalog.copy(tags = catalog.tags + root + child))
        assertFailsWith<IllegalArgumentException> {
            validateCatalogSnapshot(catalog.copy(tags = catalog.tags + root + child + CatalogTag("leaf", "Coin", "LOCATION", "child")))
        }
        assertFailsWith<IllegalArgumentException> {
            validateCatalogSnapshot(catalog.copy(tags = catalog.tags + CatalogTag("wrong", "Lieu", "LOCATION", "tag")))
        }
    }

    @Test
    fun decodesPublishedSnapshot() {
        val snapshot = decodeCatalogSnapshot(
            """{"formatVersion":1,"revision":3,"tags":[{"id":"tag","label":"Jeu","group":"TYPE","iconId":"legacy"}],"equipment":[{"id":"ball","label":"Ballon","iconId":"ball"}],"activities":[{"id":"activity","name":"Jeu","description":"","minAgeMonths":0,"maxAgeMonths":12,"durationMinutes":5,"steps":["Jouer"],"variants":[],"tagIds":["tag"],"equipmentIds":["ball"],"iconId":"legacy"}]}""",
        )
        assertEquals(3L, snapshot.revision)
        assertEquals("ball", snapshot.equipment.single().iconId)
        assertEquals(null, snapshot.activities.single().image)
    }

    @Test
    fun rejectsInvalidOrOversizedJson() {
        assertFailsWith<SerializationException> { decodeCatalogSnapshot("{}") }
        assertFailsWith<IllegalArgumentException> { decodeCatalogSnapshot(" ".repeat(2_000_001)) }
    }
}
