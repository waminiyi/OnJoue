package com.sotopatrick.onjoue.data.repositories.review

import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.models.review.*
import com.sotopatrick.onjoue.data.repositories.common.requireAffectedRow
import com.sotopatrick.onjoue.data.sources.room.AppDatabase
import com.sotopatrick.onjoue.data.sources.room.DatabaseOperation
import com.sotopatrick.onjoue.data.sources.room.databaseAccess
import com.sotopatrick.onjoue.data.sources.room.mapStorageErrors
import com.sotopatrick.onjoue.data.sources.room.review.toEntity
import com.sotopatrick.onjoue.data.sources.room.review.toModel
import kotlinx.coroutines.flow.map

class RoomReviewRepository(private val database: AppDatabase) : ReviewRepository {
    private val dao = database.reviewDao()
    override fun observeReviews(activityId: ActivityId) =
        dao.observe(activityId.id).map { rows -> rows.map { it.toModel() } }.mapStorageErrors()
    override suspend fun getReview(id: ReviewId): Review? = databaseAccess { dao.get(id.id)?.toModel() }
    override suspend fun save(review: Review) =
        databaseAccess(DatabaseOperation.UPDATE) { dao.save(review.toEntity()) }
    override suspend fun deleteReview(id: ReviewId) =
        databaseAccess(DatabaseOperation.DELETE) { requireAffectedRow(dao.delete(id.id)) }
}
