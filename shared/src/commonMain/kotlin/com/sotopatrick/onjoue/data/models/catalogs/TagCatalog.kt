package com.sotopatrick.onjoue.data.models.catalogs

import com.sotopatrick.onjoue.data.models.tag.Tag
import com.sotopatrick.onjoue.data.models.tag.TagId

class TagCatalog(tags: Collection<Tag>) {
    private val byId: Map<TagId, Tag> = tags.associateBy { it.id }
    private val children: Map<TagId, Set<TagId>> =
        tags.filter { it.parent != null }
            .groupBy({ it.parent!! }, { it.id })
            .mapValues { it.value.toSet() }

    operator fun get(id: TagId): Tag? = byId[id]

    fun expanded(ids: Set<TagId>): Set<TagId> = ids + ids.flatMap { children[it].orEmpty() }
}
