package com.sotopatrick.onjoue.data.models.avis

import com.sotopatrick.onjoue.data.models.activite.IdActivite

data class Avis(
    val id: IdAvis,
    val idActivite: IdActivite,
    val note: Note,
    val commentaire: String,
)
