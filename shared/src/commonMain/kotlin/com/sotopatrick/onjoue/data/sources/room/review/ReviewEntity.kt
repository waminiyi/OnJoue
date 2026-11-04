package com.sotopatrick.onjoue.data.sources.room.review

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sotopatrick.onjoue.data.sources.room.activity.ActivityEntity

@Entity(
    tableName = "reviews",
    foreignKeys = [ForeignKey(ActivityEntity::class, ["id"], ["activityId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("activityId")],
)
data class ReviewEntity(@PrimaryKey val id: String, val activityId: String, val rating: String, val comment: String)
