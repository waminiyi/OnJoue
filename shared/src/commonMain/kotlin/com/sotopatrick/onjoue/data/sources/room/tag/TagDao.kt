package com.sotopatrick.onjoue.data.sources.room.tag

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY normalizedLabel")
    fun observe(): Flow<List<TagEntity>>
    @Query("""
        SELECT tags.*, COUNT(DISTINCT links.activityId) AS activityCount
        FROM tags
        LEFT JOIN tags AS children ON children.parent = tags.id
        LEFT JOIN activity_tags AS links ON links.tagId = tags.id OR links.tagId = children.id
        GROUP BY tags.id
        ORDER BY tags.normalizedLabel
    """)
    fun observeUsage(): Flow<List<StoredTagUsage>>
    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun get(id: String): TagEntity?
    @Query("SELECT EXISTS(SELECT 1 FROM tags WHERE parent = :id)")
    suspend fun hasChildren(id: String): Boolean
    @Insert
    suspend fun insert(tag: TagEntity)
    @Update
    suspend fun update(tag: TagEntity): Int
    @Transaction
    suspend fun save(tag: TagEntity) {
        if (update(tag) == 0) insert(tag)
    }
    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun delete(id: String): Int
}
