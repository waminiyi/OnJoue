package com.sotopatrick.onjoue.presentation.activity.listing

import com.sotopatrick.onjoue.data.models.activity.Activity
import com.sotopatrick.onjoue.data.models.tag.Tag
import com.sotopatrick.onjoue.presentation.activity.favorites.IdeaFavoritesState
import com.sotopatrick.onjoue.presentation.errors.Message

data class ActivitySheetsState(
    val activities: List<Activity> = emptyList(),
    val loading: Boolean = true,
    val error: Message? = null,
    val locations: List<Tag> = emptyList(),
    val favorites: IdeaFavoritesState = IdeaFavoritesState(),
)
