package com.sotopatrick.onjoue.data.sources.room.tag

import androidx.room.Embedded
import com.sotopatrick.onjoue.data.models.tag.TagUsage

data class StoredTagUsage(@Embedded val tag: TagEntity, val activityCount: Int)

internal fun StoredTagUsage.toModel() = TagUsage(tag.toModel(), activityCount)
