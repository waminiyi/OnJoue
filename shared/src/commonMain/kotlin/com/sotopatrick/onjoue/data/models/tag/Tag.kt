package com.sotopatrick.onjoue.data.models.tag

data class Tag(
    val id: TagId,
    val groupe: GroupeTag,
    val libelle: String,
    val parent: TagId? = null,
)
