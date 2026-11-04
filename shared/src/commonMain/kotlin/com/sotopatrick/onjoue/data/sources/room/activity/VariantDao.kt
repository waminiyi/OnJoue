package com.sotopatrick.onjoue.data.sources.room.activity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface VariantDao {
    @Insert
    suspend fun insert(variants: List<VariantEntity>)
    @Query("DELETE FROM variants WHERE activityId = :activityId")
    suspend fun deleteByActivity(activityId: String)
}
