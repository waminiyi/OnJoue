package com.sotopatrick.onjoue.data.repositories.activity

import com.sotopatrick.onjoue.data.models.activity.*
import com.sotopatrick.onjoue.data.models.common.AppException
import com.sotopatrick.onjoue.data.models.common.ValidationReason
import com.sotopatrick.onjoue.data.models.equipment.EquipmentId
import com.sotopatrick.onjoue.data.models.tag.TagId
import com.sotopatrick.onjoue.data.repositories.common.requireAffectedRow
import com.sotopatrick.onjoue.data.repositories.common.requireDebug
import com.sotopatrick.onjoue.data.sources.room.AppDatabase
import com.sotopatrick.onjoue.data.sources.room.DatabaseOperation
import com.sotopatrick.onjoue.data.sources.room.activity.ActivityEquipmentEntity
import com.sotopatrick.onjoue.data.sources.room.activity.ActivityTagEntity
import com.sotopatrick.onjoue.data.sources.room.activity.StepEntity
import com.sotopatrick.onjoue.data.sources.room.activity.VariantEntity
import com.sotopatrick.onjoue.data.sources.room.activity.toEntity
import com.sotopatrick.onjoue.data.sources.room.activity.toModel
import com.sotopatrick.onjoue.data.sources.room.databaseAccess
import com.sotopatrick.onjoue.data.sources.room.mapStorageErrors
import com.sotopatrick.onjoue.data.sources.room.transaction
import kotlinx.coroutines.flow.map

class RoomActivityRepository(
    private val database: AppDatabase,
    private val debug: Boolean = false,
) : ActivityRepository {
    private val dao = database.activityDao()
    private val steps = database.stepDao()
    private val variants = database.variantDao()
    private val tags = database.activityTagDao()
    private val equipment = database.activityEquipmentDao()
    override fun observeActivities() = dao.observe().map { rows -> rows.map { it.toModel() } }.mapStorageErrors()
    override suspend fun getActivity(id: ActivityId): Activity? = databaseAccess { dao.get(id.id)?.toModel() }
    override suspend fun isTagUsed(id: TagId) = databaseAccess { tags.isTagUsed(id.value) }
    override suspend fun isEquipmentUsed(id: EquipmentId) = databaseAccess { equipment.isEquipmentUsed(id.value) }

    override suspend fun save(activity: Activity) {
        requireDebug(debug)
        database.transaction(DatabaseOperation.UPDATE) {
            validate(activity)
            dao.save(activity.toEntity())
            steps.deleteByActivity(activity.id.id)
            variants.deleteByActivity(activity.id.id)
            tags.deleteByActivity(activity.id.id)
            equipment.deleteByActivity(activity.id.id)
            insertRelations(activity)
        }
    }

    private fun validate(activity: Activity) {
        if (activity.name.isBlank() || activity.steps.any { it.text.isBlank() || (it.timerSeconds ?: 1) <= 0 }) {
            throw AppException.Validation(ValidationReason.INVALID_ACTIVITY)
        }
    }

    private suspend fun insertRelations(activity: Activity) {
        val id = activity.id.id
        steps.insert(activity.steps.mapIndexed { position, step -> StepEntity(id, position, step.text, step.timerSeconds) })
        variants.insert(activity.variants.mapIndexed { position, text -> VariantEntity(id, position, text) })
        tags.insert(activity.tags.map { ActivityTagEntity(id, it.id.value) })
        equipment.insert(activity.equipment.map { ActivityEquipmentEntity(id, it.id.value) })
    }

    override suspend fun deleteActivity(id: ActivityId) {
        requireDebug(debug)
        database.transaction(DatabaseOperation.DELETE) { requireAffectedRow(dao.delete(id.id)) }
    }
}
