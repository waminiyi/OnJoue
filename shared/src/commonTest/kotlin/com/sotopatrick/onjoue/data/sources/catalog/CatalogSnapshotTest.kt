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
                tagIds = listOf("tag"), equipmentIds = listOf("ball"), iconId = "future-icon",
            ),
        ),
    )

    @Test
    fun acceptsUnknownIcon() {
        validateCatalogSnapshot(catalog)
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
            """{"formatVersion":1,"revision":3,"tags":[],"equipment":[],"activities":[]}""",
        )
        assertEquals(3L, snapshot.revision)
    }

    @Test
    fun rejectsInvalidOrOversizedJson() {
        assertFailsWith<SerializationException> { decodeCatalogSnapshot("{}") }
        assertFailsWith<IllegalArgumentException> { decodeCatalogSnapshot(" ".repeat(2_000_001)) }
    }
}
