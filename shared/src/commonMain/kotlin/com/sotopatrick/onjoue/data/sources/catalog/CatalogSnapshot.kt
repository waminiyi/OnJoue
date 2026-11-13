package com.sotopatrick.onjoue.data.sources.catalog

import com.sotopatrick.onjoue.data.models.tag.TagGroup
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val MAX_CATALOG_CHARACTERS = 2_000_000
private const val MAX_CATALOG_ITEMS = 10_000

@Serializable
internal data class CatalogSnapshot(
    val formatVersion: Int,
    val revision: Long,
    val tags: List<CatalogTag>,
    val equipment: List<CatalogEquipment>,
    val activities: List<CatalogActivity>,
)

@Serializable
internal data class CatalogTag(
    val id: String,
    val label: String,
    val group: String,
    val parentId: String? = null,
)

@Serializable
internal data class CatalogEquipment(
    val id: String,
    val label: String,
    val iconId: String,
)

@Serializable
internal data class CatalogActivity(
    val id: String,
    val name: String,
    val description: String,
    val note: String? = null,
    val minAgeMonths: Int,
    val maxAgeMonths: Int,
    val durationMinutes: Int,
    val steps: List<String>,
    val variants: List<String>,
    val tagIds: List<String>,
    val equipmentIds: List<String>,
    val iconId: String,
    val document: String? = null,
)

internal fun decodeCatalogSnapshot(json: String): CatalogSnapshot {
    require(json.length <= MAX_CATALOG_CHARACTERS) { "Catalog is too large." }
    return Json.decodeFromString<CatalogSnapshot>(json).also(::validateCatalogSnapshot)
}

internal fun validateCatalogSnapshot(catalog: CatalogSnapshot) {
    require(catalog.formatVersion == 1) { "Unsupported catalog format." }
    require(catalog.revision > 0) { "Catalog revision must be positive." }
    require(
        catalog.tags.size <= MAX_CATALOG_ITEMS &&
            catalog.equipment.size <= MAX_CATALOG_ITEMS &&
            catalog.activities.size <= MAX_CATALOG_ITEMS,
    ) { "Catalog contains too many items." }
    requireUniqueIds(catalog.tags.map { it.id })
    requireUniqueIds(catalog.equipment.map { it.id })
    requireUniqueIds(catalog.activities.map { it.id })

    val tags = catalog.tags.associateBy { it.id }
    val equipmentIds = catalog.equipment.mapTo(mutableSetOf()) { it.id }
    require(catalog.tags.all { it.label.isNotBlank() }) { "Catalog contains an empty tag label." }
    require(catalog.tags.map { it.group to it.label.trim().lowercase() }.distinct().size == catalog.tags.size) {
        "Catalog contains duplicate tag labels."
    }
    require(catalog.equipment.all { it.label.isNotBlank() && it.iconId.isNotBlank() }) {
        "Catalog contains equipment without a label or icon."
    }
    require(catalog.equipment.map { it.label.trim().lowercase() }.distinct().size == catalog.equipment.size) {
        "Catalog contains duplicate equipment labels."
    }

    catalog.tags.forEach { tag ->
        val group = TagGroup.valueOf(tag.group)
        tag.parentId?.let { parentId ->
            val parent = tags[parentId]
            require(group == TagGroup.LOCATION && parent?.group == TagGroup.LOCATION.name && parent.parentId == null) {
                "Catalog contains an invalid tag hierarchy."
            }
        }
    }
    catalog.activities.forEach { activity ->
        require(activity.name.isNotBlank() && activity.iconId.isNotBlank()) {
            "Catalog contains an activity without a name or icon."
        }
        require(activity.minAgeMonths >= 0 && activity.maxAgeMonths >= activity.minAgeMonths) {
            "Catalog contains an invalid age range."
        }
        require(activity.durationMinutes > 0) { "Catalog contains an invalid duration." }
        require(activity.steps.all(String::isNotBlank) && activity.variants.all(String::isNotBlank)) {
            "Catalog contains an empty step or variant."
        }
        requireUniqueReferences(activity.tagIds, tags.keys)
        requireUniqueReferences(activity.equipmentIds, equipmentIds)
    }
}

private fun requireUniqueIds(ids: List<String>) {
    require(ids.all(String::isNotBlank) && ids.distinct().size == ids.size) {
        "Catalog contains an empty or duplicate identifier."
    }
}

private fun requireUniqueReferences(ids: List<String>, knownIds: Set<String>) {
    require(ids.distinct().size == ids.size && ids.all { it in knownIds }) {
        "Catalog contains a duplicate or unknown reference."
    }
}
