package com.sotopatrick.onjoue.data.sources.room.tag

import com.sotopatrick.onjoue.data.models.tag.Tag
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.data.models.tag.TagId
import com.sotopatrick.onjoue.data.sources.room.decodeStoredData

internal fun TagEntity.toModel(): Tag = decodeStoredData {
    require(label.isNotBlank())
    Tag(TagId(id), TagGroup.valueOf(groupName), label, parent?.let(::TagId))
}
internal fun Tag.toEntity() = TagEntity(id.value, label, group.name, parent?.value, label.trim().lowercase())
