package com.sotopatrick.onjoue.data.models.recherche

import com.sotopatrick.onjoue.data.models.activite.AgeMois
import com.sotopatrick.onjoue.data.models.activite.DureeMinutes
import com.sotopatrick.onjoue.data.models.materiel.MaterielId
import com.sotopatrick.onjoue.data.models.tag.GroupeTag
import com.sotopatrick.onjoue.data.models.tag.TagId

data class FiltreIdee(
    val ageEnfant: AgeMois,
    val dureeMax: DureeMinutes? = null,
    val materielDisponible: Set<MaterielId>? = null, // null = ne pas filtrer
    val tagsChoisis: Map<GroupeTag, Set<TagId>> = emptyMap(),
)
