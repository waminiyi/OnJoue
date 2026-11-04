package com.sotopatrick.onjoue.data.models.equipment

import kotlin.jvm.JvmInline

@JvmInline
value class EquipmentId(val value: String) {
    init {
        require(value.isNotBlank()) { "MaterielId vide" }
    }
}
