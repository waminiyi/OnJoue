package com.sotopatrick.onjoue.data.models.recherche

import com.sotopatrick.onjoue.data.models.catalogues.CatalogueTags
import com.sotopatrick.onjoue.data.models.activite.Activite
fun Activite.correspond(filtre: FiltreIdee, catalogue: CatalogueTags): Boolean {
    if (filtre.ageEnfant !in age) return false
    if (filtre.dureeMax != null && duree > filtre.dureeMax) return false
    if (filtre.materielDisponible != null && !filtre.materielDisponible.containsAll(materiel)) return false

    val tagsFiche = tags.mapNotNull(catalogue::get)
    return filtre.tagsChoisis.all { (groupe, choisis) ->
        if (choisis.isEmpty()) return@all true
        val duGroupe = tagsFiche.filter { it.groupe == groupe }
        if (duGroupe.isEmpty()) return@all true
        val acceptes = catalogue.etendu(choisis)
        duGroupe.any { it.id in acceptes }
    }
}
