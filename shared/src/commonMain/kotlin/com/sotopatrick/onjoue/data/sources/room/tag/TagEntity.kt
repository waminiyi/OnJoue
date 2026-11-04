package com.sotopatrick.onjoue.data.sources.room.tag

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tags",
    foreignKeys = [ForeignKey(
        entity = TagEntity::class, parentColumns = ["id"], childColumns = ["parent"],
        onDelete = ForeignKey.RESTRICT,
    )],
    indices = [Index("parent"), Index(value = ["groupName", "normalizedLabel"], unique = true)],
)
data class TagEntity(
    @PrimaryKey val id: String,
    val label: String,
    val groupName: String,
    val parent: String?,
    @ColumnInfo(defaultValue = "''") val normalizedLabel: String,
)
