package com.sotopatrick.onjoue.presentation.activity

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.sotopatrick.onjoue.data.models.activity.Activity
import com.sotopatrick.onjoue.data.models.tag.Tag
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.designsystem.components.BackgroundTone
import com.sotopatrick.onjoue.designsystem.components.BadgeTone
import com.sotopatrick.onjoue.designsystem.components.CardElevation
import com.sotopatrick.onjoue.designsystem.components.DsBackground
import com.sotopatrick.onjoue.designsystem.components.DsBadge
import com.sotopatrick.onjoue.designsystem.components.DsButton
import com.sotopatrick.onjoue.designsystem.components.DsCard
import com.sotopatrick.onjoue.designsystem.components.DsErrorState
import com.sotopatrick.onjoue.designsystem.components.DsFilterChip
import com.sotopatrick.onjoue.designsystem.components.DsIconToggleButton
import com.sotopatrick.onjoue.designsystem.components.DsIllustration
import com.sotopatrick.onjoue.designsystem.components.DsOutlinedIconButton
import com.sotopatrick.onjoue.designsystem.components.DsProgressIndicator
import com.sotopatrick.onjoue.designsystem.components.DsScreenHeader
import com.sotopatrick.onjoue.designsystem.components.DsSearchField
import com.sotopatrick.onjoue.designsystem.components.DsText
import com.sotopatrick.onjoue.designsystem.components.DsTextButton
import com.sotopatrick.onjoue.designsystem.components.IllustrationStyle
import com.sotopatrick.onjoue.designsystem.components.TextRole
import com.sotopatrick.onjoue.designsystem.components.TextTone
import com.sotopatrick.onjoue.designsystem.foundation.Spacing
import onjoue.shared.generated.resources.Res
import onjoue.shared.generated.resources.ic_favorite_filled
import onjoue.shared.generated.resources.ic_favorites
import onjoue.shared.generated.resources.ic_filter
import org.jetbrains.compose.resources.painterResource

@Composable
fun ActivitySheetsScreen(
    state: ActivitySheetsState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onSelectActivity: (String) -> Unit = {},
    onToggleFavorite: (String) -> Unit = {},
    onRetryFavorites: () -> Unit = {},
) {
    DsBackground(modifier.fillMaxSize(), tone = BackgroundTone.Mint) {
        if (state.activities.isEmpty()) {
            IdeaCatalogState(state, onRetry)
        } else {
            IdeaListing(state, onRetry, onSelectActivity, onToggleFavorite, onRetryFavorites)
        }
    }
}

@Composable
private fun IdeaCatalogState(state: ActivitySheetsState, onRetry: () -> Unit) {
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

@Composable
private fun IdeaListing(
    state: ActivitySheetsState,
    onRetry: () -> Unit,
    onSelectActivity: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRetryFavorites: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf(AgeFilter.ALL) }
    var locationId by rememberSaveable { mutableStateOf<String?>(null) }
    var shortDuration by rememberSaveable { mutableStateOf(false) }
    val filters = IdeaFilters(query, age, locationId, shortDuration)
    val results = state.activities.filter(filters::matches)
    val reset = { query = ""; age = AgeFilter.ALL; locationId = null; shortDuration = false }
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("ideas-list"),
        contentPadding = PaddingValues(Spacing.ExtraLarge),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        item(key = "header") {
            DsScreenHeader("Des idées à partager", subtitle = "Pour jouer, bouger et grandir ensemble.")
        }
        item(key = "search") {
            DsSearchField(query, onValueChange = { query = it }, label = "Rechercher une idée")
        }
        item(key = "ages") {
            QuickAgeFilters(age, onSelect = { age = it })
        }
        item(key = "quick-filters") {
            QuickIdeaFilters(
                state.locations, locationId, shortDuration,
                onLocationChange = { locationId = it },
                onDurationChange = { shortDuration = !shortDuration },
            )
        }
        item(key = "advanced-filters") { AdvancedFiltersPlaceholder() }
        item(key = "feedback") {
            IdeaFeedback(state, onRetry, onRetryFavorites)
        }
        item(key = "count") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DsText(if (filters.active) "Résultats" else "À découvrir", role = TextRole.TitleMedium)
                DsText(
                    "${results.size} ${if (results.size == 1) "idée" else "idées"}",
                    role = TextRole.LabelLarge,
                    tone = TextTone.Secondary,
                )
            }
        }
        if (filters.active) {
            item(key = "reset") { DsTextButton("Retirer les filtres", onClick = reset) }
        }
        if (results.isEmpty()) {
            item(key = "empty") {
                IdeaStatus(
                    "Aucune idée pour cette recherche",
                    "Essayez avec moins de filtres ou un autre mot pour laisser de nouvelles idées arriver.",
                )
            }
        }
        items(results, key = { it.id.id }) { activity ->
            IdeaCard(activity, state.favorites, onSelectActivity, onToggleFavorite)
        }
    }
}

@Composable
private fun QuickAgeFilters(selected: AgeFilter, onSelect: (AgeFilter) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
    ) {
        AgeFilter.entries.forEach { filter ->
            DsFilterChip(selected == filter, filter.label, onClick = { onSelect(filter) })
        }
    }
}

@Composable
private fun QuickIdeaFilters(
    locations: List<Tag>,
    locationId: String?,
    shortDuration: Boolean,
    onLocationChange: (String?) -> Unit,
    onDurationChange: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
    ) {
        locations.forEach { location ->
            val selected = location.id.value == locationId
            DsFilterChip(
                selected, location.label,
                onClick = { onLocationChange(if (selected) null else location.id.value) },
            )
        }
        DsFilterChip(shortDuration, "15 min ou moins", onClick = onDurationChange)
    }
}

@Composable
private fun AdvancedFiltersPlaceholder() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
    ) {
        DsOutlinedIconButton(
            painterResource(Res.drawable.ic_filter),
            contentDescription = "Filtres avancés, bientôt disponibles",
            onClick = {},
            enabled = false,
        )
        DsText("Plus de filtres · Bientôt disponible", role = TextRole.BodySmall, tone = TextTone.Secondary)
    }
}

@Composable
private fun IdeaFeedback(
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
        state.favorites.notice?.let { DsText(it.content, role = TextRole.BodyMedium, tone = TextTone.Primary) }
    }
}

@Composable
private fun IdeaCard(
    activity: Activity,
    favorites: IdeaFavoritesState,
    onSelectActivity: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val selected = activity.id in favorites.entries
    DsCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onSelectActivity(activity.id.id) },
        elevation = CardElevation.Low,
    ) {
        Column(Modifier.padding(Spacing.MediumPlus), verticalArrangement = Arrangement.spacedBy(Spacing.Small)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                DsIllustration(activityEmoji(activity), style = IllustrationStyle.Plain)
                DsText(activity.name, Modifier.weight(1f), role = TextRole.TitleMedium)
                DsIconToggleButton(
                    checked = selected,
                    icon = painterResource(
                        if (selected) Res.drawable.ic_favorite_filled else Res.drawable.ic_favorites,
                    ),
                    contentDescription = if (selected) "Retirer des favoris" else "Ajouter aux favoris",
                    onCheckedChange = { onToggleFavorite(activity.id.id) },
                    enabled = favorites.ready && favorites.busy == null,
                )
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
            ) {
                DsBadge(ageRangeLabel(activity), tone = BadgeTone.Plain)
                DsBadge("${activity.duration.value} min", tone = BadgeTone.Plain)
                activity.tags.firstOrNull { it.group == TagGroup.MOOD }?.let {
                    DsBadge(it.label, tone = BadgeTone.Plain)
                }
            }
        }
    }
}

@Composable
private fun IdeaStatus(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier.padding(Spacing.XXLarge),
        verticalArrangement = Arrangement.spacedBy(Spacing.Large, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (loading) DsProgressIndicator()
        DsText(title, role = TextRole.HeadlineSmall, textAlign = TextAlign.Center)
        DsText(description, role = TextRole.BodyLarge, tone = TextTone.Secondary, textAlign = TextAlign.Center)
        if (actionLabel != null) DsButton(actionLabel, onClick = onAction)
    }
}
