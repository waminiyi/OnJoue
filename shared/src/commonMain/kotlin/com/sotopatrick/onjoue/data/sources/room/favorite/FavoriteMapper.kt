package com.sotopatrick.onjoue.data.sources.room.favorite

import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.models.favorite.Favorite
import com.sotopatrick.onjoue.data.models.favorite.FavoriteId
import com.sotopatrick.onjoue.data.sources.room.decodeStoredData

internal fun FavoriteEntity.toModel(): Favorite = decodeStoredData { Favorite(FavoriteId(id), ActivityId(activityId)) }
internal fun Favorite.toEntity() = FavoriteEntity(id.id, activityId.id)
