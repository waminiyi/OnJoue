package com.sotopatrick.onjoue.presentation.activity.listing.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sotopatrick.onjoue.data.models.tag.Tag
import com.sotopatrick.onjoue.designsystem.components.DsBottomSheet
import com.sotopatrick.onjoue.designsystem.components.DsButton
import com.sotopatrick.onjoue.designsystem.components.DsFilterChip
import com.sotopatrick.onjoue.designsystem.components.DsText
import com.sotopatrick.onjoue.designsystem.components.DsTextButton
import com.sotopatrick.onjoue.designsystem.components.TextRole
import com.sotopatrick.onjoue.designsystem.foundation.Spacing

@Composable
internal fun IdeaFilterSheet(
    filters: IdeaFilters,
    onFiltersChange: (IdeaFilters) -> Unit,
    locations: List<Tag>,
    onDismiss: () -> Unit,
) {
    DsBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(Spacing.ExtraLarge),
            verticalArrangement = Arrangement.spacedBy(Spacing.Large),
        ) {
            DsText("Filtres", role = TextRole.TitleLarge)
            FilterOptions(
                title = "Âge",
                options = AgeFilter.entries.map { it to it.label },
                selected = filters.age,
                onSelect = { onFiltersChange(filters.copy(age = it)) },
            )
            FilterOptions(
                title = "Lieu",
                options = listOf<Pair<String?, String>>(null to "Lieu") +
                    locations.map { it.id.value to it.label },
                selected = filters.locationId,
                onSelect = { onFiltersChange(filters.copy(locationId = it)) },
            )
            DsText("Durée", role = TextRole.TitleSmall)
            DsFilterChip(
                selected = filters.shortDuration,
                label = "≤ 15 min",
                onClick = { onFiltersChange(filters.copy(shortDuration = !filters.shortDuration)) },
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DsTextButton("Réinitialiser", onClick = { onFiltersChange(IdeaFilters()) })
                DsButton("Appliquer", onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun <T> FilterOptions(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    DsText(title, role = TextRole.TitleSmall)
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
    ) {
        options.forEach { (value, label) ->
            DsFilterChip(
                selected = selected == value,
                label = label,
                onClick = { onSelect(value) },
            )
        }
    }
}
