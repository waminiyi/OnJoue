package com.sotopatrick.onjoue.data.sources.room.tried

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TriedDao {
    @Query("SELECT * FROM tried_activities ORDER BY id")
    fun observe(): Flow<List<TriedEntity>>
    @Query("SELECT EXISTS(SELECT 1 FROM tried_activities WHERE activityId = :activityId)")
    suspend fun isTried(activityId: String): Boolean
    @Insert
    suspend fun insert(tried: TriedEntity)
    @Query("DELETE FROM tried_activities WHERE id = :id")
    suspend fun delete(id: String): Int
}
