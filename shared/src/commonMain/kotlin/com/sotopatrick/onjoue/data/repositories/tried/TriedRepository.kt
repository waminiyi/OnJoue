package com.sotopatrick.onjoue.data.repositories.tried

import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.models.tried.Tried
import com.sotopatrick.onjoue.data.models.tried.TriedId
import kotlinx.coroutines.flow.Flow

interface TriedRepository {
    fun observeTried(): Flow<List<Tried>>
    suspend fun isTried(activityId: ActivityId): Boolean
    suspend fun addTried(tried: Tried)
    suspend fun deleteTried(id: TriedId)
}
