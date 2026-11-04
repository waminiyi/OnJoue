package com.sotopatrick.onjoue.data.repositories.equipment

import com.sotopatrick.onjoue.data.models.equipment.Equipment
import com.sotopatrick.onjoue.data.models.equipment.EquipmentId
import com.sotopatrick.onjoue.data.models.equipment.EquipmentUsage
import kotlinx.coroutines.flow.Flow

interface EquipmentRepository {
    fun observeEquipment(): Flow<List<Equipment>>
    fun observeEquipmentUsage(): Flow<List<EquipmentUsage>>
    suspend fun getEquipment(id: EquipmentId): Equipment?
    suspend fun save(equipment: Equipment)
    suspend fun deleteEquipment(id: EquipmentId)
}
