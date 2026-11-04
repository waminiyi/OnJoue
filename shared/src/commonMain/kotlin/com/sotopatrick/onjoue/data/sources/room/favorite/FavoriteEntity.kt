package com.sotopatrick.onjoue.data.sources.room.favorite

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sotopatrick.onjoue.data.sources.room.activity.ActivityEntity

@Entity(
    tableName = "favorites",
    foreignKeys = [ForeignKey(ActivityEntity::class, ["id"], ["activityId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["activityId"], unique = true)],
)
data class FavoriteEntity(@PrimaryKey val id: String, val activityId: String)
