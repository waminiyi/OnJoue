package com.sotopatrick.onjoue.data.sources.room.activity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val note: String?,
    val minAge: Int,
    val maxAge: Int,
    val duration: Int,
    val image: String?,
    val document: String?,
)
