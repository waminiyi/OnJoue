package com.sotopatrick.onjoue.data.repositories.tag

import com.sotopatrick.onjoue.data.models.common.AppException
import com.sotopatrick.onjoue.data.models.common.ValidationReason
import com.sotopatrick.onjoue.data.models.tag.*
import com.sotopatrick.onjoue.data.repositories.common.normalizedDisplayLabel
import com.sotopatrick.onjoue.data.repositories.common.requireAffectedRow
import com.sotopatrick.onjoue.data.repositories.common.requireDebug
import com.sotopatrick.onjoue.data.sources.room.AppDatabase
import com.sotopatrick.onjoue.data.sources.room.DatabaseOperation
import com.sotopatrick.onjoue.data.sources.room.databaseAccess
import com.sotopatrick.onjoue.data.sources.room.mapStorageErrors
import com.sotopatrick.onjoue.data.sources.room.tag.toEntity
import com.sotopatrick.onjoue.data.sources.room.tag.toModel
import com.sotopatrick.onjoue.data.sources.room.transaction
import kotlinx.coroutines.flow.map

class RoomTagRepository(
    private val database: AppDatabase,
    private val debug: Boolean = false,
) : TagRepository {
    private val dao = database.tagDao()

    override fun observeTags() = dao.observe().map { rows -> rows.map { it.toModel() } }.mapStorageErrors()
    override fun observeTagUsage() = dao.observeUsage().map { rows -> rows.map { it.toModel() } }.mapStorageErrors()
    override suspend fun getTag(id: TagId): Tag? = databaseAccess { dao.get(id.value)?.toModel() }

    override suspend fun save(tag: Tag) {
        requireDebug(debug)
        database.transaction(DatabaseOperation.UPDATE) {
            val normalizedTag = tag.copy(label = normalizedDisplayLabel(tag.label))
            validateHierarchy(normalizedTag)
            dao.save(normalizedTag.toEntity())
        }
    }

    private suspend fun validateHierarchy(tag: Tag) {
        if (dao.hasChildren(tag.id.value) && (tag.group != TagGroup.LOCATION || tag.parent != null)) {
            throw AppException.Validation(ValidationReason.INVALID_HIERARCHY)
        }
        val parentId = tag.parent ?: return
        val parent = dao.get(parentId.value)?.toModel() ?: throw AppException.InvalidReference()
        if (tag.id == parentId || tag.group != TagGroup.LOCATION ||
            parent.group != TagGroup.LOCATION || parent.parent != null
        ) throw AppException.Validation(ValidationReason.INVALID_HIERARCHY)
    }

    override suspend fun deleteTag(id: TagId) {
        requireDebug(debug)
        database.transaction(DatabaseOperation.DELETE) {
            requireAffectedRow(dao.delete(id.value))
        }
    }
}
