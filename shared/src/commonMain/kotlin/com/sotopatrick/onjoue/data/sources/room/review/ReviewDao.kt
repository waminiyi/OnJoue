package com.sotopatrick.onjoue.data.sources.room.review

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE activityId = :activityId ORDER BY id")
    fun observe(activityId: String): Flow<List<ReviewEntity>>
    @Query("SELECT * FROM reviews WHERE id = :id")
    suspend fun get(id: String): ReviewEntity?
    @Insert
    suspend fun insert(review: ReviewEntity)
    @Update
    suspend fun update(review: ReviewEntity): Int
    @Transaction
    suspend fun save(review: ReviewEntity) {
        if (update(review) == 0) insert(review)
    }
    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun delete(id: String): Int
}
