package com.sotopatrick.onjoue.data.sources.room.review

import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.models.review.Rating
import com.sotopatrick.onjoue.data.models.review.Review
import com.sotopatrick.onjoue.data.models.review.ReviewId
import com.sotopatrick.onjoue.data.sources.room.decodeStoredData

internal fun ReviewEntity.toModel(): Review = decodeStoredData {
    Review(ReviewId(id), ActivityId(activityId), Rating.valueOf(rating), comment)
}
internal fun Review.toEntity() = ReviewEntity(id.id, activityId.id, rating.name, comment)
