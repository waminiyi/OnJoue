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
    val reset = { age = AgeFilter.ALL; locationId = null; shortDuration = false }
    IdeaListingContent(
        state = state,
        results = results,
        age = age,
        onAgeSelect = { age = it },
        locationId = locationId,
        onLocationSelect = { locationId = it },
        shortDuration = shortDuration,
        onDurationToggle = { shortDuration = !shortDuration },
        onOpenFilters = { showSheet = true },
        onRetry = onRetry,
        onSelectActivity = onSelectActivity,
        onToggleFavorite = onToggleFavorite,
        onRetryFavorites = onRetryFavorites,
    )
    if (showSheet) {
        IdeaFilterSheet(
            age = age,
            onAgeSelect = { age = it },
            locations = state.locations,
            locationId = locationId,
            onLocationSelect = { locationId = it },
            shortDuration = shortDuration,
            onDurationToggle = { shortDuration = !shortDuration },
            onReset = reset,
            onDismiss = { showSheet = false },
        )
    }
}

@Composable
private fun IdeaListingContent(
    state: ActivitySheetsState,
    results: List<Activity>,
    age: AgeFilter,
    onAgeSelect: (AgeFilter) -> Unit,
    locationId: String?,
    onLocationSelect: (String?) -> Unit,
    shortDuration: Boolean,
    onDurationToggle: () -> Unit,
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
                age = age,
                onAgeSelect = onAgeSelect,
                locations = state.locations,
                locationId = locationId,
                onLocationSelect = onLocationSelect,
                shortDuration = shortDuration,
                onDurationToggle = onDurationToggle,
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
    age: AgeFilter,
    onAgeSelect: (AgeFilter) -> Unit,
    locations: List<Tag>,
    locationId: String?,
    onLocationSelect: (String?) -> Unit,
    shortDuration: Boolean,
    onDurationToggle: () -> Unit,
    onOpenFilters: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val ageOptions = AgeFilter.entries.map { it to it.label }
        DsFilterDropdown(
            label = age.label,
            options = ageOptions,
            onSelect = onAgeSelect,
            selected = age != AgeFilter.ALL,
        )
        val locationLabel = locations.firstOrNull { it.id.value == locationId }?.label ?: "Lieu"
        val locationOptions = listOf<Pair<String?, String>>(null to "Lieu") +
            locations.map { it.id.value to it.label }
        DsFilterDropdown(
            label = locationLabel,
            options = locationOptions,
            onSelect = onLocationSelect,
            selected = locationId != null,
        )
        DsFilterChip(
            selected = shortDuration,
            label = "≤ 15 min",
            onClick = onDurationToggle,
        )
        val activeCount = (if (age != AgeFilter.ALL) 1 else 0) +
            (if (locationId != null) 1 else 0) +
            (if (shortDuration) 1 else 0)
        DsFilterIconButton(
            icon = painterResource(Res.drawable.ic_filter),
            contentDescription = "Filtres avancés",
            onClick = onOpenFilters,
            activeCount = activeCount,
        )
    }
}
