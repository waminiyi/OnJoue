package com.sotopatrick.onjoue.data.sources.room.favorite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY id")
    fun observe(): Flow<List<FavoriteEntity>>
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE activityId = :activityId)")
    suspend fun isFavorite(activityId: String): Boolean
    @Insert
    suspend fun insert(favorite: FavoriteEntity)
    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun delete(id: String): Int
}
