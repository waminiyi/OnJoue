package com.sotopatrick.onjoue.data.models.tried

import com.sotopatrick.onjoue.data.models.activity.ActivityId

data class Tried(
    val id: TriedId,
    val activityId: ActivityId,
)
