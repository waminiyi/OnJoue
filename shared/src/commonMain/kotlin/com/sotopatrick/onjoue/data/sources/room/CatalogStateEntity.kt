package com.sotopatrick.onjoue.data.sources.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_state")
data class CatalogStateEntity(
    @PrimaryKey val id: Int = 1,
    val catalogVersion: Int,
)
