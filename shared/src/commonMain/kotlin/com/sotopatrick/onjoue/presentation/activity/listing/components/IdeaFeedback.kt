package com.sotopatrick.onjoue.presentation.activity.listing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.sotopatrick.onjoue.designsystem.components.DsErrorState
import com.sotopatrick.onjoue.designsystem.components.DsProgressIndicator
import com.sotopatrick.onjoue.designsystem.components.DsText
import com.sotopatrick.onjoue.designsystem.components.TextRole
import com.sotopatrick.onjoue.designsystem.components.TextTone
import com.sotopatrick.onjoue.designsystem.foundation.Spacing
import com.sotopatrick.onjoue.presentation.activity.listing.ActivitySheetsState

@Composable
internal fun IdeaFeedback(
    state: ActivitySheetsState,
    onRetry: () -> Unit,
    onRetryFavorites: () -> Unit,
) {
    Column(
        Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(Spacing.Small),
    ) {
        if (state.loading) DsProgressIndicator()
        if (state.favorites.busy != null) {
            DsText("Mise à jour des favoris…", role = TextRole.BodySmall, tone = TextTone.Secondary)
        } else if (!state.favorites.ready && state.favorites.error == null) {
            DsText("Chargement des favoris…", role = TextRole.BodySmall, tone = TextTone.Secondary)
        }
        state.error?.let { DsErrorState(it.content, onRetry) }
        state.favorites.error?.let {
            DsErrorState(
                "Favoris : ${it.content}",
                onRetryFavorites,
                retryLabel = "Réessayer les favoris",
            )
        }
    }
}
