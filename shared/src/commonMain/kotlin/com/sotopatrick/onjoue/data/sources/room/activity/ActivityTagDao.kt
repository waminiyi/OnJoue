package com.sotopatrick.onjoue.data.sources.room.activity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ActivityTagDao {
    @Insert
    suspend fun insert(tags: List<ActivityTagEntity>)
    @Query("DELETE FROM activity_tags WHERE activityId = :activityId")
    suspend fun deleteByActivity(activityId: String)
    @Query("SELECT EXISTS(SELECT 1 FROM activity_tags WHERE tagId = :tagId)")
    suspend fun isTagUsed(tagId: String): Boolean
}
