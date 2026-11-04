package com.sotopatrick.onjoue.presentation.management

import com.sotopatrick.onjoue.data.models.equipment.EquipmentUsage
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.data.models.tag.TagUsage
import com.sotopatrick.onjoue.data.repositories.sampleEquipment
import com.sotopatrick.onjoue.data.repositories.sampleTag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManagementListTest {
    private val inside = sampleTag("Dedans", TagGroup.LOCATION)
    private val living = sampleTag("Salon", TagGroup.LOCATION, inside.id)
    private val kitchen = sampleTag("Cuisine", TagGroup.LOCATION, inside.id)
    private val game = sampleTag("Jeu", TagGroup.TYPE)
    private val calm = sampleTag("Calme", TagGroup.MOOD)
    private val ball = sampleEquipment("Ballon")
    private val chair = sampleEquipment("Chaise")
    private val data = ManagementData(
        listOf(TagUsage(living, 2), TagUsage(calm, 0), TagUsage(game, 3), TagUsage(inside, 4), TagUsage(kitchen, 1)),
        listOf(EquipmentUsage(chair, 1), EquipmentUsage(ball, 2)),
    )

    @Test
    fun groupsFollowCatalogOrderAndChildrenFollowTheirParent() {
        val groups = managementGroups(data, ManagementCategory.TAGS, "", null)
        assertEquals(listOf("Type", "Lieu", "Ambiance"), groups.map { it.label })
        assertEquals(listOf("Dedans", "Cuisine", "Salon"), groups[1].rows.map { it.label })
        assertEquals(listOf(4, 1, 2), groups[1].rows.map { it.activityCount })
        assertEquals("Dedans", groups[1].rows.last().parentLabel)
    }

    @Test
    fun searchAndGroupFilterRetainParentMetadataAndRealCounts() {
        val groups = managementGroups(data, ManagementCategory.TAGS, " SAL ", TagGroup.LOCATION)
        assertEquals(1, groups.size)
        assertEquals(ManagementRow(living.id.value, "Salon", 2, "Dedans"), groups.single().rows.single())
        assertTrue(managementGroups(data, ManagementCategory.TAGS, "Salon", TagGroup.TYPE).isEmpty())
    }

    @Test
    fun equipmentIsSortedAndFilteredWithoutApplyingTagGroups() {
        val groups = managementGroups(data, ManagementCategory.EQUIPMENT, "", TagGroup.TYPE)
        assertEquals(listOf("Ballon", "Chaise"), groups.single().rows.map { it.label })
        assertEquals(listOf(2, 1), groups.single().rows.map { it.activityCount })
        assertEquals("Ballon", managementGroups(data, ManagementCategory.EQUIPMENT, " bal ", null).single().rows.single().label)
    }
}
