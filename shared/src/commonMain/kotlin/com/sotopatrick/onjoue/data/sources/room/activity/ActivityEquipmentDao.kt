package com.sotopatrick.onjoue.data.sources.room.activity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ActivityEquipmentDao {
    @Insert
    suspend fun insert(equipment: List<ActivityEquipmentEntity>)
    @Query("DELETE FROM activity_equipment WHERE activityId = :activityId")
    suspend fun deleteByActivity(activityId: String)
    @Query("SELECT EXISTS(SELECT 1 FROM activity_equipment WHERE equipmentId = :equipmentId)")
    suspend fun isEquipmentUsed(equipmentId: String): Boolean
}
