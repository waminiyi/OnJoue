package com.sotopatrick.onjoue.data.sources.room.activity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StepDao {
    @Insert
    suspend fun insert(steps: List<StepEntity>)
    @Query("DELETE FROM steps WHERE activityId = :activityId")
    suspend fun deleteByActivity(activityId: String)
}
