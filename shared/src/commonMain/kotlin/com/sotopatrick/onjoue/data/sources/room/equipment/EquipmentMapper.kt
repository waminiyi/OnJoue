package com.sotopatrick.onjoue.data.sources.room.equipment

import com.sotopatrick.onjoue.data.models.equipment.Equipment
import com.sotopatrick.onjoue.data.models.equipment.EquipmentId
import com.sotopatrick.onjoue.data.sources.room.decodeStoredData

internal fun EquipmentEntity.toModel(): Equipment = decodeStoredData {
    require(label.isNotBlank())
    Equipment(EquipmentId(id), label)
}
internal fun Equipment.toEntity() = EquipmentEntity(id.value, label, label.trim().lowercase())
