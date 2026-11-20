package com.sotopatrick.onjoue.presentation.activity.details

import com.sotopatrick.onjoue.data.models.activity.Activity
import com.sotopatrick.onjoue.data.models.favorite.FavoriteId
import com.sotopatrick.onjoue.data.models.tried.TriedId
import com.sotopatrick.onjoue.presentation.errors.Message

data class ActivityDetailsState(
    val activity: Activity? = null,
    val isFavorite: Boolean = false,
    val isTried: Boolean = false,
    val favoriteId: FavoriteId? = null,
    val triedId: TriedId? = null,
    val loading: Boolean = true,
    val busy: Boolean = false,
    val error: Message? = null,
    val notice: Message? = null,
)
