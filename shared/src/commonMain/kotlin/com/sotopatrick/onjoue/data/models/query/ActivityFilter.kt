package com.sotopatrick.onjoue.data.models.query

import com.sotopatrick.onjoue.data.models.activity.AgeMonths
import com.sotopatrick.onjoue.data.models.activity.DurationMinutes
import com.sotopatrick.onjoue.data.models.equipment.EquipmentId
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.data.models.tag.TagId

data class ActivityFilter(
    val childAge: AgeMonths,
    val maxDuration: DurationMinutes? = null,
    val availableEquipment: Set<EquipmentId>? = null,
    val selectedTags: Map<TagGroup, Set<TagId>> = emptyMap(),
)
