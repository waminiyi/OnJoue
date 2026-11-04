package com.sotopatrick.onjoue.data.sources.room.activity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sotopatrick.onjoue.data.sources.room.tag.TagEntity

@Entity(
    tableName = "activity_tags", primaryKeys = ["activityId", "tagId"],
    foreignKeys = [
        ForeignKey(ActivityEntity::class, ["id"], ["activityId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(TagEntity::class, ["id"], ["tagId"], onDelete = ForeignKey.RESTRICT),
    ], indices = [Index("tagId")],
)
data class ActivityTagEntity(val activityId: String, val tagId: String)
