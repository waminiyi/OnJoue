package com.sotopatrick.onjoue.data.sources.room.activity

import com.sotopatrick.onjoue.data.models.activity.Activity
import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.models.activity.AgeMonths
import com.sotopatrick.onjoue.data.models.activity.AgeRange
import com.sotopatrick.onjoue.data.models.activity.DurationMinutes
import com.sotopatrick.onjoue.data.models.activity.Step
import com.sotopatrick.onjoue.data.models.common.Url
import com.sotopatrick.onjoue.data.sources.room.decodeStoredData
import com.sotopatrick.onjoue.data.sources.room.equipment.toModel
import com.sotopatrick.onjoue.data.sources.room.tag.toModel

internal fun StoredActivity.toModel(): Activity = decodeStoredData {
    require(activity.name.isNotBlank())
    require(steps.all { it.text.isNotBlank() && (it.timerSeconds ?: 1) > 0 })
    Activity(
        id = ActivityId(activity.id), name = activity.name, description = activity.description,
        note = activity.note, age = AgeRange(AgeMonths(activity.minAge), AgeMonths(activity.maxAge)),
        duration = DurationMinutes(activity.duration),
        image = activity.image?.let(::Url), document = activity.document?.let(::Url),
        steps = steps.sortedBy { it.position }.map { Step(it.text, it.timerSeconds) },
        variants = variants.sortedBy { it.position }.map { it.text },
        tags = tags.map { it.toModel() }.toSet(),
        equipment = equipment.map { it.toModel() }.toSet(),
    )
}

internal fun Activity.toEntity() = ActivityEntity(
    id.id, name, description, note, age.min.value, age.max.value, duration.value, image?.value, document?.value,
)
