package com.sotopatrick.onjoue.data.repositories.tag

import com.sotopatrick.onjoue.data.models.tag.Tag
import com.sotopatrick.onjoue.data.models.tag.TagId
import com.sotopatrick.onjoue.data.models.tag.TagUsage
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun observeTags(): Flow<List<Tag>>
    fun observeTagUsage(): Flow<List<TagUsage>>
    suspend fun getTag(id: TagId): Tag?
    suspend fun save(tag: Tag)
    suspend fun deleteTag(id: TagId)
}
