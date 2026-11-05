package com.sotopatrick.onjoue.data.sources.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CatalogStateDao {
    @Query("SELECT catalogVersion FROM catalog_state WHERE id = 1")
    suspend fun importedVersion(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(state: CatalogStateEntity)
}
