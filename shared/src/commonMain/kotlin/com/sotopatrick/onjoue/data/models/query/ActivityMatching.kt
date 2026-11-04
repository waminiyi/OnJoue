package com.sotopatrick.onjoue.data.models.query

import com.sotopatrick.onjoue.data.models.activity.Activity
import com.sotopatrick.onjoue.data.models.catalogs.TagCatalog

fun Activity.matches(filter: ActivityFilter, catalog: TagCatalog): Boolean {
    if (filter.childAge !in age) return false
    if (filter.maxDuration != null && duration > filter.maxDuration) return false
    if (filter.availableEquipment != null && !filter.availableEquipment.containsAll(equipment.map { it.id })) return false

    return filter.selectedTags.all { (group, selected) ->
        if (selected.isEmpty()) return@all true
        val groupTags = tags.filter { it.group == group }
        if (groupTags.isEmpty()) return@all true
        val accepted = catalog.expanded(selected)
        groupTags.any { it.id in accepted }
    }
}
