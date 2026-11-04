package com.sotopatrick.onjoue.data.models.favorite

import com.sotopatrick.onjoue.data.models.activity.ActivityId

data class Favorite(
    val id: FavoriteId,
    val activityId: ActivityId,)
