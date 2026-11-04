package com.sotopatrick.onjoue.data.repositories.review

import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.models.review.Review
import com.sotopatrick.onjoue.data.models.review.ReviewId
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun observeReviews(activityId: ActivityId): Flow<List<Review>>
    suspend fun getReview(id: ReviewId): Review?
    suspend fun save(review: Review)
    suspend fun deleteReview(id: ReviewId)
}
