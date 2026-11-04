package com.sotopatrick.onjoue.data.models.review

import com.sotopatrick.onjoue.data.models.activity.ActivityId

data class Review(
    val id: ReviewId,
    val activityId: ActivityId,
    val rating: Rating,
    val comment: String,
)
