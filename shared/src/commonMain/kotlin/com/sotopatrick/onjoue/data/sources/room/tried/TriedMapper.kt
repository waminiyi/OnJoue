package com.sotopatrick.onjoue.data.sources.room.tried

import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.models.tried.Tried
import com.sotopatrick.onjoue.data.models.tried.TriedId
import com.sotopatrick.onjoue.data.sources.room.decodeStoredData

internal fun TriedEntity.toModel(): Tried = decodeStoredData { Tried(TriedId(id), ActivityId(activityId)) }
internal fun Tried.toEntity() = TriedEntity(id.id, activityId.id)
