package com.sotopatrick.onjoue.data.models.tag


data class Tag(
    val id: TagId,
    val group: TagGroup,
    val label: String,
    val parent: TagId? = null,
)
