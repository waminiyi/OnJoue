package com.sotopatrick.onjoue.presentation.activity.listing.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.sotopatrick.onjoue.data.models.activity.Activity
import com.sotopatrick.onjoue.data.models.tag.Tag
import com.sotopatrick.onjoue.designsystem.components.DsFilterChip
import com.sotopatrick.onjoue.designsystem.components.DsFilterDropdown
import com.sotopatrick.onjoue.designsystem.components.DsFilterIconButton
import com.sotopatrick.onjoue.designsystem.components.DsScreenHeader
import com.sotopatrick.onjoue.designsystem.foundation.Spacing
import com.sotopatrick.onjoue.presentation.activity.listing.ActivitySheetsState
import onjoue.shared.generated.resources.Res
import onjoue.shared.generated.resources.ic_filter
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun IdeaListing(
    state: ActivitySheetsState,
    onRetry: () -> Unit,
    onSelectActivity: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRetryFavorites: () -> Unit,
) {
    var age by rememberSaveable { mutableStateOf(AgeFilter.ALL) }
    var locationId by rememberSaveable { mutableStateOf<String?>(null) }
    var shortDuration by rememberSaveable { mutableStateOf(false) }
    var showSheet by rememberSaveable { mutableStateOf(false) }
    val filters = IdeaFilters(age, locationId, shortDuration)
    val results = state.activities.filter(filters::matches)
    val updateFilters: (IdeaFilters) -> Unit = {
        age = it.age
        locationId = it.locationId
        shortDuration = it.shortDuration
    }
    IdeaListingContent(
        state = state,
        results = results,
        filters = filters,
        onFiltersChange = updateFilters,
        onOpenFilters = { showSheet = true },
        onRetry = onRetry,
        onSelectActivity = onSelectActivity,
        onToggleFavorite = onToggleFavorite,
        onRetryFavorites = onRetryFavorites,
    )
    if (showSheet) {
        IdeaFilterSheet(
            filters = filters,
            onFiltersChange = updateFilters,
            locations = state.locations,
            onDismiss = { showSheet = false },
        )
    }
}

@Composable
private fun IdeaListingContent(
    state: ActivitySheetsState,
    results: List<Activity>,
    filters: IdeaFilters,
    onFiltersChange: (IdeaFilters) -> Unit,
    onOpenFilters: () -> Unit,
    onRetry: () -> Unit,
    onSelectActivity: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRetryFavorites: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("ideas-list"),
        contentPadding = PaddingValues(Spacing.ExtraLarge),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        item(key = "header") {
            DsScreenHeader("Des idées à partager", subtitle = "Pour jouer, bouger et grandir ensemble.")
        }
        item(key = "single-line-filters") {
            SingleLineFilters(
                filters = filters,
                onFiltersChange = onFiltersChange,
                locations = state.locations,
                onOpenFilters = onOpenFilters,
            )
        }
        item(key = "feedback") {
            IdeaFeedback(state, onRetry, onRetryFavorites)
        }
        if (results.isEmpty()) {
            item(key = "empty") {
                IdeaStatus(
                    "Aucune idée pour cette recherche",
                    "Essayez avec moins de filtres pour laisser de nouvelles idées arriver.",
                )
            }
        }
        items(results, key = { it.id.id }) { activity ->
            IdeaCard(activity, state.favorites, onSelectActivity, onToggleFavorite)
        }
    }
}

@Composable
private fun SingleLineFilters(
    filters: IdeaFilters,
    onFiltersChange: (IdeaFilters) -> Unit,
    locations: List<Tag>,
    onOpenFilters: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val ageOptions = AgeFilter.entries.map { it to it.label }
        DsFilterDropdown(
            label = filters.age.label,
            options = ageOptions,
            onSelect = { onFiltersChange(filters.copy(age = it)) },
            selected = filters.age != AgeFilter.ALL,
        )
        val locationLabel = locations.firstOrNull { it.id.value == filters.locationId }?.label ?: "Lieu"
        val locationOptions = listOf<Pair<String?, String>>(null to "Lieu") +
            locations.map { it.id.value to it.label }
        DsFilterDropdown(
            label = locationLabel,
            options = locationOptions,
            onSelect = { onFiltersChange(filters.copy(locationId = it)) },
            selected = filters.locationId != null,
        )
        DsFilterChip(
            selected = filters.shortDuration,
            label = "≤ 15 min",
            onClick = { onFiltersChange(filters.copy(shortDuration = !filters.shortDuration)) },
        )
        val activeCount = listOf(
            filters.age != AgeFilter.ALL,
            filters.locationId != null,
            filters.shortDuration,
        ).count { it }
        DsFilterIconButton(
            icon = painterResource(Res.drawable.ic_filter),
            contentDescription = "Filtres avancés",
            onClick = onOpenFilters,
            activeCount = activeCount,
        )
    }
}
