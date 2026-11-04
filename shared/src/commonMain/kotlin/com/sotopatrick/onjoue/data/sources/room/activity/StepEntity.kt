package com.sotopatrick.onjoue.data.sources.room.activity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "steps", primaryKeys = ["activityId", "position"],
    foreignKeys = [ForeignKey(ActivityEntity::class, ["id"], ["activityId"], onDelete = ForeignKey.CASCADE)],
)
data class StepEntity(val activityId: String, val position: Int, val text: String, val timerSeconds: Int?)
