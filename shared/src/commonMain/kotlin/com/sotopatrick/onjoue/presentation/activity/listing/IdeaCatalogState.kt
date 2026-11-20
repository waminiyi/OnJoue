package com.sotopatrick.onjoue.presentation.activity.listing

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sotopatrick.onjoue.presentation.activity.listing.components.IdeaStatus

@Composable
internal fun IdeaCatalogState(state: ActivitySheetsState, onRetry: () -> Unit) {
    val title = when {
        state.loading -> "Les idées arrivent…"
        state.error != null -> "Les idées n’ont pas pu arriver"
        else -> "Les idées se préparent"
    }
    IdeaStatus(
        title = title,
        description = when {
            state.loading -> "Encore un instant pour trouver de quoi jouer ensemble."
            state.error != null -> state.error.content
            else -> "Aucune idée pour le moment. Revenez un peu plus tard pour faire le plein d’inspiration."
        },
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        loading = state.loading,
        actionLabel = if (state.loading) null else "Réessayer",
        onAction = onRetry,
    )
}
