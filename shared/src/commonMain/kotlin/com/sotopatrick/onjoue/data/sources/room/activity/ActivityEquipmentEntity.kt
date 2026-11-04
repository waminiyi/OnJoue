package com.sotopatrick.onjoue.data.sources.room.activity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sotopatrick.onjoue.data.sources.room.equipment.EquipmentEntity

@Entity(
    tableName = "activity_equipment", primaryKeys = ["activityId", "equipmentId"],
    foreignKeys = [
        ForeignKey(ActivityEntity::class, ["id"], ["activityId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(EquipmentEntity::class, ["id"], ["equipmentId"], onDelete = ForeignKey.RESTRICT),
    ], indices = [Index("equipmentId")],
)
data class ActivityEquipmentEntity(val activityId: String, val equipmentId: String)
