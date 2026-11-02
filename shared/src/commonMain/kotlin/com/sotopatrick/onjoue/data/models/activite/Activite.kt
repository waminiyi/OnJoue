package com.sotopatrick.onjoue.data.models.activite

import com.sotopatrick.onjoue.data.models.commun.Url
import com.sotopatrick.onjoue.data.models.materiel.MaterielId
import com.sotopatrick.onjoue.data.models.tag.TagId

data class Activite(
    val id: IdActivite,
    val nom: String,
    val description: String,
    val etapes: List<Etape> = emptyList(),
    val note: String? = null,
    val variantes: List<String> = emptyList(),
    val age: PlageAge,
    val duree: DureeMinutes,
    val materiel: Set<MaterielId> = emptySet(),
    val image: Url? = null,
    val document: Url? = null,
    val tags: Set<TagId> = emptySet(),
)
