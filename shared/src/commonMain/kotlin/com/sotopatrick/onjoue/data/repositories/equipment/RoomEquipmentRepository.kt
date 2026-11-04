package com.sotopatrick.onjoue.data.repositories.equipment

import com.sotopatrick.onjoue.data.models.equipment.*
import com.sotopatrick.onjoue.data.repositories.common.normalizedDisplayLabel
import com.sotopatrick.onjoue.data.repositories.common.requireAffectedRow
import com.sotopatrick.onjoue.data.repositories.common.requireDebug
import com.sotopatrick.onjoue.data.sources.room.AppDatabase
import com.sotopatrick.onjoue.data.sources.room.DatabaseOperation
import com.sotopatrick.onjoue.data.sources.room.databaseAccess
import com.sotopatrick.onjoue.data.sources.room.equipment.toEntity
import com.sotopatrick.onjoue.data.sources.room.equipment.toModel
import com.sotopatrick.onjoue.data.sources.room.mapStorageErrors
import com.sotopatrick.onjoue.data.sources.room.transaction
import kotlinx.coroutines.flow.map

class RoomEquipmentRepository(
    private val database: AppDatabase,
    private val debug: Boolean = false,
) : EquipmentRepository {
    private val dao = database.equipmentDao()
    override fun observeEquipment() = dao.observe().map { rows -> rows.map { it.toModel() } }.mapStorageErrors()
    override fun observeEquipmentUsage() = dao.observeUsage().map { rows -> rows.map { it.toModel() } }.mapStorageErrors()
    override suspend fun getEquipment(id: EquipmentId): Equipment? = databaseAccess { dao.get(id.value)?.toModel() }

    override suspend fun save(equipment: Equipment) {
        requireDebug(debug)
        database.transaction(DatabaseOperation.UPDATE) {
            dao.save(equipment.copy(label = normalizedDisplayLabel(equipment.label)).toEntity())
        }
    }

    override suspend fun deleteEquipment(id: EquipmentId) {
        requireDebug(debug)
        database.transaction(DatabaseOperation.DELETE) {
            requireAffectedRow(dao.delete(id.value))
        }
    }
}
