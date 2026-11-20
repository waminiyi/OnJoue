package com.sotopatrick.onjoue.presentation.activity.listing.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
    age: AgeFilter,
    onAgeSelect: (AgeFilter) -> Unit,
    locations: List<Tag>,
    locationId: String?,
    onLocationSelect: (String?) -> Unit,
    shortDuration: Boolean,
    onDurationToggle: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    DsBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(Spacing.ExtraLarge),
            verticalArrangement = Arrangement.spacedBy(Spacing.Large),
        ) {
            DsText("Filtres", role = TextRole.TitleLarge)
            DsText("Âge", role = TextRole.TitleSmall)
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
            ) {
                AgeFilter.entries.forEach { option ->
                    DsFilterChip(
                        selected = age == option,
                        label = option.label,
                        onClick = { onAgeSelect(option) },
                    )
                }
            }
            DsText("Lieu", role = TextRole.TitleSmall)
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
            ) {
                DsFilterChip(
                    selected = locationId == null,
                    label = "Lieu",
                    onClick = { onLocationSelect(null) },
                )
                locations.forEach { location ->
                    DsFilterChip(
                        selected = locationId == location.id.value,
                        label = location.label,
                        onClick = { onLocationSelect(location.id.value) },
                    )
                }
            }
            DsText("Durée", role = TextRole.TitleSmall)
            DsFilterChip(
                selected = shortDuration,
                label = "≤ 15 min",
                onClick = onDurationToggle,
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DsTextButton("Réinitialiser", onClick = onReset)
                DsButton("Appliquer", onClick = onDismiss)
            }
        }
    }
}
