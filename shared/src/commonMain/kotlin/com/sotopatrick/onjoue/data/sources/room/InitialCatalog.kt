package com.sotopatrick.onjoue.data.sources.room

import com.sotopatrick.onjoue.data.models.common.AppException
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.data.sources.room.activity.ActivityEntity
import com.sotopatrick.onjoue.data.sources.room.activity.ActivityEquipmentEntity
import com.sotopatrick.onjoue.data.sources.room.activity.ActivityTagEntity
import com.sotopatrick.onjoue.data.sources.room.activity.StepEntity
import com.sotopatrick.onjoue.data.sources.room.activity.VariantEntity
import com.sotopatrick.onjoue.data.sources.room.equipment.EquipmentEntity
import com.sotopatrick.onjoue.data.sources.room.tag.TagEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import onjoue.shared.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

private const val INITIAL_CATALOG_PATH = "files/initial-catalog.json"

@Serializable
private data class InitialCatalogJson(
    val version: Int,
    val tags: List<InitialTagJson>,
    val equipment: List<InitialEquipmentJson>,
    val activities: List<InitialActivityJson>,
)

@Serializable
private data class InitialTagJson(
    val id: String,
    val label: String,
    val group: String,
    val parentId: String? = null,
)

@Serializable
private data class InitialEquipmentJson(
    val id: String,
    val label: String,
    val iconId: String = "",
)

@Serializable
private data class InitialActivityJson(
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
    val document: String? = null,
    val image: String? = null,
)

private val catalogJson = Json { ignoreUnknownKeys = false }

@OptIn(ExperimentalResourceApi::class)
suspend fun seedInitialCatalog(database: AppDatabase, forceReset: Boolean = false) {
    val catalog = try {
        catalogJson.decodeFromString<InitialCatalogJson>(
            Res.readBytes(INITIAL_CATALOG_PATH).decodeToString(),
        )
    } catch (exception: SerializationException) {
        throw IllegalStateException("The initial activity catalog is invalid.", exception)
    }
    validateCatalog(catalog)

    database.transaction(DatabaseOperation.INSERT) {
        if (!forceReset && database.catalogStateDao().importedVersion() == catalog.version) return@transaction
        if (forceReset) database.replaceCatalogWith(catalog) else database.mergeInitialCatalog(catalog)
        database.catalogStateDao().save(CatalogStateEntity(catalogVersion = catalog.version))
    }
}

private suspend fun AppDatabase.replaceCatalogWith(catalog: InitialCatalogJson) {
    val catalogActivityIds = catalog.activities.mapTo(mutableSetOf()) { it.id }
    activityDao().ids().filterNot(catalogActivityIds::contains).forEach { id ->
        activityDao().delete(id)
    }
    activityTagDao().deleteAll()
    activityEquipmentDao().deleteAll()
    tagDao().clearParents()
    tagDao().deleteAll()
    equipmentDao().deleteAll()
    mergeInitialCatalog(catalog)
}

private suspend fun AppDatabase.mergeInitialCatalog(catalog: InitialCatalogJson) {
    val tagIds = importTags(catalog.tags)
    val equipmentIds = importEquipment(catalog.equipment)

    catalog.activities.forEach { activity ->
        activityDao().save(
            ActivityEntity(
                id = activity.id,
                name = activity.name,
                description = activity.description,
                note = activity.note,
                minAge = activity.minAgeMonths,
                maxAge = activity.maxAgeMonths,
                duration = activity.durationMinutes,
                image = activity.image,
                document = activity.document,
            ),
        )
        stepDao().deleteByActivity(activity.id)
        variantDao().deleteByActivity(activity.id)
        activityTagDao().deleteByActivity(activity.id)
        activityEquipmentDao().deleteByActivity(activity.id)
        if (activity.steps.isNotEmpty()) {
            stepDao().insert(
                activity.steps.mapIndexed { index, text ->
                    StepEntity(activity.id, index, text, timerSeconds = null)
                },
            )
        }
        if (activity.variants.isNotEmpty()) {
            variantDao().insert(
                activity.variants.mapIndexed { index, text ->
                    VariantEntity(activity.id, index, text)
                },
            )
        }
        if (activity.tagIds.isNotEmpty()) {
            activityTagDao().insert(
                activity.tagIds.map { tagId ->
                    ActivityTagEntity(activity.id, tagIds.getValue(tagId))
                },
            )
        }
        if (activity.equipmentIds.isNotEmpty()) {
            activityEquipmentDao().insert(
                activity.equipmentIds.map { equipmentId ->
                    ActivityEquipmentEntity(activity.id, equipmentIds.getValue(equipmentId))
                },
            )
        }
    }
}

private suspend fun AppDatabase.importTags(tags: List<InitialTagJson>): Map<String, String> {
    val actualIds = mutableMapOf<String, String>()
    val pending = tags.toMutableList()
    while (pending.isNotEmpty()) {
        val ready = pending.filter { it.parentId == null || it.parentId in actualIds }
        require(ready.isNotEmpty()) { "Catalog tag hierarchy contains a cycle." }
        ready.forEach { tag ->
            val normalizedLabel = tag.label.trim().lowercase()
            val existing = tagDao().get(tag.id)
            if (existing != null &&
                (existing.groupName != tag.group || existing.normalizedLabel != normalizedLabel)
            ) {
                throw AppException.Conflict()
            }
            val actualId = existing?.id
                ?: tagDao().getByNormalizedLabel(tag.group, normalizedLabel)?.id
                ?: tag.id
            tagDao().save(
                TagEntity(
                    id = actualId,
                    label = tag.label.trim(),
                    groupName = tag.group,
                    parent = tag.parentId?.let(actualIds::get),
                    normalizedLabel = normalizedLabel,
                ),
            )
            actualIds[tag.id] = actualId
            pending.remove(tag)
        }
    }
    return actualIds
}

private suspend fun AppDatabase.importEquipment(
    equipment: List<InitialEquipmentJson>,
): Map<String, String> = equipment.associate { item ->
    val normalizedLabel = item.label.trim().lowercase()
    val existing = equipmentDao().get(item.id)
    if (existing != null && existing.normalizedLabel != normalizedLabel) {
        throw AppException.Conflict()
    }
    val actualId = existing?.id
        ?: equipmentDao().getByNormalizedLabel(normalizedLabel)?.id
        ?: item.id
    equipmentDao().save(EquipmentEntity(actualId, item.label.trim(), normalizedLabel, item.iconId))
    item.id to actualId
}.toMap()

private fun validateCatalog(catalog: InitialCatalogJson) {
    require(catalog.version > 0) { "Catalog version must be positive." }
    requireUniqueIds(catalog.tags.map { it.id }, "tag")
    requireUniqueIds(catalog.equipment.map { it.id }, "equipment")
    requireUniqueIds(catalog.activities.map { it.id }, "activity")

    val tagIds = catalog.tags.mapTo(mutableSetOf()) { it.id }
    val equipmentIds = catalog.equipment.mapTo(mutableSetOf()) { it.id }
    catalog.tags.forEach { tag ->
        require(tag.label.isNotBlank()) { "Tag ${tag.id} has no label." }
        TagGroup.valueOf(tag.group)
        require(tag.parentId == null || tag.parentId in tagIds) {
            "Tag ${tag.id} references an unknown parent."
        }
    }
    catalog.activities.forEach { activity ->
        require(activity.name.isNotBlank()) { "Activity ${activity.id} has no name." }
        require(activity.minAgeMonths >= 0 && activity.maxAgeMonths >= activity.minAgeMonths) {
            "Activity ${activity.id} has an invalid age range."
        }
        require(activity.durationMinutes > 0) { "Activity ${activity.id} has an invalid duration." }
        require(activity.steps.all(String::isNotBlank)) { "Activity ${activity.id} has an empty step." }
        require(activity.image == null || activity.image.startsWith("https://")) {
            "Activity ${activity.id} has an invalid image URL."
        }
        require(activity.tagIds.all { it in tagIds }) {
            "Activity ${activity.id} references an unknown tag."
        }
        require(activity.equipmentIds.all { it in equipmentIds }) {
            "Activity ${activity.id} references unknown equipment."
        }
    }
}

private fun requireUniqueIds(ids: List<String>, type: String) {
    require(ids.all(String::isNotBlank) && ids.distinct().size == ids.size) {
        "Catalog contains an empty or duplicate $type id."
    }
}
