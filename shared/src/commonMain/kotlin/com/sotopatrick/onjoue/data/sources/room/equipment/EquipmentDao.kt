package com.sotopatrick.onjoue.data.sources.room.equipment

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment ORDER BY normalizedLabel")
    fun observe(): Flow<List<EquipmentEntity>>
    @Query("""
        SELECT equipment.*, COUNT(links.activityId) AS activityCount
        FROM equipment
        LEFT JOIN activity_equipment AS links ON links.equipmentId = equipment.id
        GROUP BY equipment.id
        ORDER BY equipment.normalizedLabel
    """)
    fun observeUsage(): Flow<List<StoredEquipmentUsage>>
    @Query("SELECT * FROM equipment WHERE id = :id")
    suspend fun get(id: String): EquipmentEntity?
    @Insert
    suspend fun insert(equipment: EquipmentEntity)
    @Update
    suspend fun update(equipment: EquipmentEntity): Int
    @Transaction
    suspend fun save(equipment: EquipmentEntity) {
        if (update(equipment) == 0) insert(equipment)
    }
    @Query("DELETE FROM equipment WHERE id = :id")
    suspend fun delete(id: String): Int
}
