package com.sotopatrick.onjoue.data.repositories.common

import com.sotopatrick.onjoue.data.models.common.AppException
import com.sotopatrick.onjoue.data.models.common.ValidationReason

internal fun requireDebug(debug: Boolean) {
    if (!debug) throw AppException.Forbidden()
}

internal fun normalizedDisplayLabel(label: String): String =
    label.trim().also {
        if (it.isBlank()) throw AppException.Validation(ValidationReason.EMPTY_LABEL)
    }

internal fun requireAffectedRow(count: Int) {
    if (count == 0) throw AppException.NotFound()
}
