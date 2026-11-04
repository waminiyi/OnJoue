package com.sotopatrick.onjoue.presentation.errors

import com.sotopatrick.onjoue.data.models.common.AppException
import com.sotopatrick.onjoue.data.models.common.ValidationReason
import kotlinx.coroutines.CancellationException

data class Message(val content: String)

fun interface ErrorParser {
    fun parse(exception: Exception): Message
}

class FrenchErrorParser : ErrorParser {
    override fun parse(exception: Exception): Message {
        if (exception is CancellationException) throw exception
        val content = when (exception) {
            is AppException.Validation -> when (exception.reason) {
                ValidationReason.EMPTY_LABEL -> "Le libellé ne peut pas être vide."
                ValidationReason.INVALID_HIERARCHY -> "Seuls les lieux peuvent avoir un parent, sur deux niveaux maximum."
                ValidationReason.INVALID_ACTIVITY -> "L'activité ou ses étapes sont invalides."
            }
            is AppException.Conflict -> "Un élément possède déjà cet identifiant ou ce libellé."
            is AppException.NotFound -> "Cet élément n'existe plus."
            is AppException.InUse -> "Cet élément est utilisé par une activité ou possède des enfants."
            is AppException.Forbidden -> "La gestion est réservée aux builds debug."
            is AppException.InvalidReference -> "Un élément associé n'existe plus."
            is AppException.CorruptData -> "Les données enregistrées sont invalides."
            is AppException.Storage -> "Impossible d'accéder aux données locales. Réessayez."
            else -> {
                println("Unexpected application error: ${exception::class.simpleName}")
                "Une erreur inattendue s'est produite. Réessayez."
            }
        }
        return Message(content)
    }
}
