package com.sotopatrick.onjoue.data.models.activity

import com.sotopatrick.onjoue.data.models.common.Url
import com.sotopatrick.onjoue.data.models.equipment.Equipment
import com.sotopatrick.onjoue.data.models.tag.Tag

data class Activity(
    val id: ActivityId,
    val name: String,
    val description: String,
    val steps: List<Step> = emptyList(),
    val note: String? = null,
    val variants: List<String> = emptyList(),
    val age: AgeRange,
    val duration: DurationMinutes,
    val equipment: Set<Equipment> = emptySet(),
    val image: Url? = null,
    val document: Url? = null,
    val tags: Set<Tag> = emptySet(),
)
