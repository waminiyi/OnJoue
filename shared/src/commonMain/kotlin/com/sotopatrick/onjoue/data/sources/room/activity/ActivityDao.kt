package com.sotopatrick.onjoue.data.sources.room.activity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Transaction
    @Query("SELECT * FROM activities ORDER BY name")
    fun observe(): Flow<List<StoredActivity>>
    @Transaction
    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun get(id: String): StoredActivity?
    @Insert
    suspend fun insert(activity: ActivityEntity)
    @Update
    suspend fun update(activity: ActivityEntity): Int
    @Transaction
    suspend fun save(activity: ActivityEntity) {
        if (update(activity) == 0) insert(activity)
    }
    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun delete(id: String): Int
}
