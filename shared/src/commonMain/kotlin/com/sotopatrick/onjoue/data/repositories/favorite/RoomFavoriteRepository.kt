package com.sotopatrick.onjoue.data.repositories.favorite

import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.models.favorite.*
import com.sotopatrick.onjoue.data.repositories.common.requireAffectedRow
import com.sotopatrick.onjoue.data.sources.room.AppDatabase
import com.sotopatrick.onjoue.data.sources.room.DatabaseOperation
import com.sotopatrick.onjoue.data.sources.room.databaseAccess
import com.sotopatrick.onjoue.data.sources.room.favorite.toEntity
import com.sotopatrick.onjoue.data.sources.room.favorite.toModel
import com.sotopatrick.onjoue.data.sources.room.mapStorageErrors
import kotlinx.coroutines.flow.map

class RoomFavoriteRepository(private val database: AppDatabase) : FavoriteRepository {
    private val dao = database.favoriteDao()
    override fun observeFavorites() = dao.observe().map { rows -> rows.map { it.toModel() } }.mapStorageErrors()
    override suspend fun isFavorite(activityId: ActivityId) = databaseAccess { dao.isFavorite(activityId.id) }
    override suspend fun addFavorite(favorite: Favorite) =
        databaseAccess(DatabaseOperation.INSERT) { dao.insert(favorite.toEntity()) }
    override suspend fun deleteFavorite(id: FavoriteId) =
        databaseAccess(DatabaseOperation.DELETE) { requireAffectedRow(dao.delete(id.id)) }
}
