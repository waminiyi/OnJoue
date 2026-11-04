package com.sotopatrick.onjoue.data.sources.room.equipment

import androidx.room.Embedded
import com.sotopatrick.onjoue.data.models.equipment.EquipmentUsage

data class StoredEquipmentUsage(@Embedded val equipment: EquipmentEntity, val activityCount: Int)

internal fun StoredEquipmentUsage.toModel() = EquipmentUsage(equipment.toModel(), activityCount)
