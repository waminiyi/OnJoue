package com.sotopatrick.onjoue.presentation.management

import com.sotopatrick.onjoue.data.models.tag.TagGroup

internal data class ManagementRow(
    val id: String,
    val label: String,
    val activityCount: Int,
    val parentLabel: String? = null,
)

internal data class ManagementGroup(val label: String?, val rows: List<ManagementRow>)

internal fun managementGroups(
    data: ManagementData,
    category: ManagementCategory,
    query: String,
    filter: TagGroup?,
): List<ManagementGroup> {
    val search = query.trim()
    if (category == ManagementCategory.EQUIPMENT) {
        return listOf(
            ManagementGroup(
                null,
                data.equipmentUsage.filter { it.equipment.label.contains(search, ignoreCase = true) }
                    .sortedBy { it.equipment.label.lowercase() }
                    .map { ManagementRow(it.equipment.id.value, it.equipment.label, it.activityCount) },
            ),
        )
    }
    val tagsById = data.tags.associateBy { it.id }
    return TagGroup.entries.filter { filter == null || it == filter }.mapNotNull { group ->
        val rows = data.tagUsage.filter {
            it.tag.group == group && it.tag.label.contains(search, ignoreCase = true)
        }.sortedWith(
            compareBy(
                { (tagsById[it.tag.parent]?.label ?: it.tag.label).lowercase() },
                { if (it.tag.parent == null) 0 else 1 },
                { it.tag.label.lowercase() },
            ),
        ).map {
            ManagementRow(it.tag.id.value, it.tag.label, it.activityCount, tagsById[it.tag.parent]?.label)
        }
        if (rows.isEmpty()) null else ManagementGroup(group.displayLabel(), rows)
    }
}

internal fun TagGroup.displayLabel(): String = when (this) {
    TagGroup.TYPE -> "Type"
    TagGroup.LOCATION -> "Lieu"
    TagGroup.OCCASION -> "Occasion"
    TagGroup.SEASON -> "Saison"
    TagGroup.TIME -> "Moment"
    TagGroup.PARTICIPANTS -> "Participants"
    TagGroup.MOOD -> "Ambiance"
}
