package com.sotopatrick.onjoue.data.repositories.activity

import com.sotopatrick.onjoue.data.models.activity.Activity
import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.models.equipment.EquipmentId
import com.sotopatrick.onjoue.data.models.tag.TagId
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    suspend fun isTagUsed(id: TagId): Boolean
    suspend fun isEquipmentUsed(id: EquipmentId): Boolean
    fun observeActivities(): Flow<List<Activity>>
    suspend fun getActivity(id: ActivityId): Activity?
    suspend fun save(activity: Activity)
    suspend fun deleteActivity(id: ActivityId)
}
