package com.sotopatrick.onjoue.data.sources.room.activity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.sotopatrick.onjoue.data.sources.room.equipment.EquipmentEntity
import com.sotopatrick.onjoue.data.sources.room.tag.TagEntity

data class StoredActivity(
    @Embedded val activity: ActivityEntity,
    @Relation(parentColumn = "id", entityColumn = "activityId") val steps: List<StepEntity>,
    @Relation(parentColumn = "id", entityColumn = "activityId") val variants: List<VariantEntity>,
    @Relation(
        parentColumn = "id", entityColumn = "id",
        associateBy = Junction(ActivityTagEntity::class, parentColumn = "activityId", entityColumn = "tagId"),
    ) val tags: List<TagEntity>,
    @Relation(
        parentColumn = "id", entityColumn = "id",
        associateBy = Junction(ActivityEquipmentEntity::class, parentColumn = "activityId", entityColumn = "equipmentId"),
    ) val equipment: List<EquipmentEntity>,
)
