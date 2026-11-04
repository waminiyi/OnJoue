package com.sotopatrick.onjoue.data.repositories.favorite

import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.models.favorite.Favorite
import com.sotopatrick.onjoue.data.models.favorite.FavoriteId
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun observeFavorites(): Flow<List<Favorite>>
    suspend fun isFavorite(activityId: ActivityId): Boolean
    suspend fun addFavorite(favorite: Favorite)
    suspend fun deleteFavorite(id: FavoriteId)
}
