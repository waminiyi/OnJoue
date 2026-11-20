package com.sotopatrick.onjoue.data.repositories.tried

import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.models.tried.Tried
import com.sotopatrick.onjoue.data.models.tried.TriedId
import com.sotopatrick.onjoue.data.repositories.common.requireAffectedRow
import com.sotopatrick.onjoue.data.sources.room.AppDatabase
import com.sotopatrick.onjoue.data.sources.room.DatabaseOperation
import com.sotopatrick.onjoue.data.sources.room.databaseAccess
import com.sotopatrick.onjoue.data.sources.room.mapStorageErrors
import com.sotopatrick.onjoue.data.sources.room.tried.toEntity
import com.sotopatrick.onjoue.data.sources.room.tried.toModel
import kotlinx.coroutines.flow.map

class RoomTriedRepository(private val database: AppDatabase) : TriedRepository {
    private val dao = database.triedDao()
    override fun observeTried() = dao.observe().map { rows -> rows.map { it.toModel() } }.mapStorageErrors()
    override suspend fun isTried(activityId: ActivityId) = databaseAccess { dao.isTried(activityId.id) }
    override suspend fun addTried(tried: Tried) =
        databaseAccess(DatabaseOperation.INSERT) { dao.insert(tried.toEntity()) }
    override suspend fun deleteTried(id: TriedId) =
        databaseAccess(DatabaseOperation.DELETE) { requireAffectedRow(dao.delete(id.id)) }
}
