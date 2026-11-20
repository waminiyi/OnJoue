package com.sotopatrick.onjoue.presentation.activity.favorites

import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.models.favorite.Favorite
import com.sotopatrick.onjoue.presentation.errors.Message

data class IdeaFavoritesState(
    val entries: Map<ActivityId, Favorite> = emptyMap(),
    val ready: Boolean = false,
    val busy: ActivityId? = null,
    val error: Message? = null,
    val notice: Message? = null,
)
