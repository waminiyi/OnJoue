package com.sotopatrick.onjoue.data.models.catalogues

import com.sotopatrick.onjoue.data.models.tag.Tag
import com.sotopatrick.onjoue.data.models.tag.TagId

class CatalogueTags(tags: Collection<Tag>) {
    private val parId: Map<TagId, Tag> = tags.associateBy { it.id }
    private val enfants: Map<TagId, Set<TagId>> =
        tags.filter { it.parent != null }
            .groupBy({ it.parent!! }, { it.id })
            .mapValues { it.value.toSet() }

    operator fun get(id: TagId): Tag? = parId[id]

    fun etendu(ids: Set<TagId>): Set<TagId> = ids + ids.flatMap { enfants[it].orEmpty() }
}
