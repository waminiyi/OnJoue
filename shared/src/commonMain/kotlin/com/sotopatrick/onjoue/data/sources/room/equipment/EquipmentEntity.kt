package com.sotopatrick.onjoue.data.sources.room.equipment

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "equipment", indices = [Index(value = ["normalizedLabel"], unique = true)])
data class EquipmentEntity(
    @PrimaryKey val id: String,
    val label: String,
    @ColumnInfo(defaultValue = "''") val normalizedLabel: String,
)
