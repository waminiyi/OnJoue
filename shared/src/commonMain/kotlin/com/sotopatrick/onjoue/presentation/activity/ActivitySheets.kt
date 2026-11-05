package com.sotopatrick.onjoue.presentation.activity

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.sotopatrick.onjoue.data.models.activity.Activity
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.designsystem.components.ActionTone
import com.sotopatrick.onjoue.designsystem.components.CardElevation
import com.sotopatrick.onjoue.designsystem.components.DsBadge
import com.sotopatrick.onjoue.designsystem.components.DsBulletItem
import com.sotopatrick.onjoue.designsystem.components.DsButton
import com.sotopatrick.onjoue.designsystem.components.DsCallout
import com.sotopatrick.onjoue.designsystem.components.DsCard
import com.sotopatrick.onjoue.designsystem.components.DsErrorState
import com.sotopatrick.onjoue.designsystem.components.DsFilterChip
import com.sotopatrick.onjoue.designsystem.components.DsHeroBanner
import com.sotopatrick.onjoue.designsystem.components.DsIllustration
import com.sotopatrick.onjoue.designsystem.components.DsLoadingState
import com.sotopatrick.onjoue.designsystem.components.DsNumberedItem
import com.sotopatrick.onjoue.designsystem.components.DsScreenHeader
import com.sotopatrick.onjoue.designsystem.components.DsSearchField
import com.sotopatrick.onjoue.designsystem.components.DsSection
import com.sotopatrick.onjoue.designsystem.components.DsText
import com.sotopatrick.onjoue.designsystem.components.DsTextButton
import com.sotopatrick.onjoue.designsystem.components.IllustrationSize
import com.sotopatrick.onjoue.designsystem.components.TextRole
import com.sotopatrick.onjoue.designsystem.components.TextTone
import com.sotopatrick.onjoue.designsystem.foundation.Spacing
import com.sotopatrick.onjoue.presentation.errors.Message

private enum class AgeFilter(val label: String, val minMonths: Int, val maxMonths: Int) {
    ALL("Toutes", 0, Int.MAX_VALUE),
    TODDLER("1–3 ans", 12, 36),
    PRESCHOOL("3–5 ans", 36, 60),
    SCHOOL("5–8 ans", 60, 96),
}

@Composable
fun ActivitySheetsScreen(
    state: ActivitySheetsState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onSelectActivity: (String) -> Unit = {},
) {
    var query by rememberSaveable { mutableStateOf("") }
    var ageFilter by rememberSaveable { mutableStateOf(AgeFilter.ALL) }
    val filteredActivities = state.activities.filter { it.matches(query.trim(), ageFilter) }

    Column(
        modifier.fillMaxSize().padding(horizontal = Spacing.ExtraLarge),
        verticalArrangement = Arrangement.spacedBy(Spacing.MediumPlus),
    ) {
        DsScreenHeader(
            title = "Fiches d’activités",
            subtitle = "Des idées simples pour jouer et bouger ensemble.",
            modifier = Modifier.padding(top = Spacing.ExtraLarge),
        )
        DsSearchField(
            value = query,
            onValueChange = { query = it },
            label = "Rechercher une activité",
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
        ) {
            AgeFilter.entries.forEach { filter ->
                DsFilterChip(selected = ageFilter == filter, label = filter.label, onClick = { ageFilter = filter })
            }
        }
        when {
            state.loading && state.activities.isEmpty() -> DsLoadingState()
            state.error != null && state.activities.isEmpty() -> DsErrorState(
                state.error.content,
                onRetry = onRetry,
                modifier = Modifier.padding(vertical = Spacing.XXLarge),
            )
            state.activities.isEmpty() -> DsText("Aucune fiche pour le moment.")
            filteredActivities.isEmpty() -> DsText(
                "Aucune activité ne correspond à cette recherche.",
                tone = TextTone.Secondary,
            )
            else -> ActivitySheetsResults(
                activities = filteredActivities,
                filtered = query.isNotBlank() || ageFilter != AgeFilter.ALL,
                error = state.error,
                onSelectActivity = onSelectActivity,
            )
        }
    }
}

private fun Activity.matches(query: String, ageFilter: AgeFilter): Boolean {
    val matchesQuery = query.isBlank() ||
        name.contains(query, ignoreCase = true) ||
        description.contains(query, ignoreCase = true) ||
        equipment.any { it.label.contains(query, ignoreCase = true) } ||
        tags.any { it.label.contains(query, ignoreCase = true) }
    val matchesAge = ageFilter == AgeFilter.ALL ||
        age.min.value <= ageFilter.maxMonths && age.max.value >= ageFilter.minMonths
    return matchesQuery && matchesAge
}

@Composable
private fun ColumnScope.ActivitySheetsResults(
    activities: List<Activity>,
    filtered: Boolean,
    error: Message?,
    onSelectActivity: (String) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DsText(if (filtered) "Résultats" else "À découvrir", role = TextRole.TitleMedium)
        DsText("${activities.size} fiches", role = TextRole.LabelLarge, tone = TextTone.Secondary)
    }
    if (error != null) DsText(error.content, tone = TextTone.Error)
    LazyColumn(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        items(activities, key = { it.id.id }) { activity ->
            ActivitySheetCard(activity, onClick = { onSelectActivity(activity.id.id) })
        }
        item { Spacer(Modifier.height(Spacing.Small)) }
    }
}

@Composable
private fun ActivitySheetCard(activity: Activity, onClick: () -> Unit) {
    DsCard(modifier = Modifier.fillMaxWidth(), onClick = onClick, elevation = CardElevation.Low) {
        Row(
            Modifier.fillMaxWidth().padding(Spacing.MediumPlus),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DsIllustration(activityEmoji(activity))
            Spacer(Modifier.width(Spacing.MediumPlus))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.XXSmallPlus)) {
                DsText(
                    activity.name,
                    role = TextRole.TitleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                DsText(
                    activity.description,
                    role = TextRole.BodyMedium,
                    tone = TextTone.Secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                ActivityBadges(activity, Spacing.ExtraSmall)
            }
        }
    }
}

@Composable
private fun ActivityBadges(activity: Activity, spacing: Dp) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        DsBadge(ageRangeLabel(activity))
        DsBadge("${activity.duration.value} min")
        activity.tags.firstOrNull { it.group == TagGroup.MOOD }?.let { DsBadge(it.label) }
    }
}

@Composable
fun ActivityDetailScreen(
    state: ActivityDetailsState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.loading && state.activity == null -> DsLoadingState(modifier)
        state.activity == null -> Column(
            modifier.fillMaxSize().padding(Spacing.XXLarge),
            verticalArrangement = Arrangement.spacedBy(Spacing.Large),
        ) {
            DsTextButton(BackLabel, onClick = onBack)
            DsText(state.error?.content ?: "Cette fiche n’est plus disponible.", tone = TextTone.Error)
            DsButton("Réessayer", onClick = onRetry)
        }
        else -> ActivityDetailContent(state.activity, onBack, modifier)
    }
}

@Composable
private fun ActivityDetailContent(activity: Activity, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.LargePlus),
    ) {
        ActivityHero(activity, onBack)
        ActivityDetailSections(activity)
    }
}

@Composable
private fun ActivityHero(activity: Activity, onBack: () -> Unit) {
    DsHeroBanner {
        DsTextButton(BackLabel, onClick = onBack, tone = ActionTone.OnPrimaryContainer)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            DsIllustration(activityEmoji(activity), size = IllustrationSize.Large)
            Spacer(Modifier.width(Spacing.Large))
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.XXSmall)) {
                DsText(activity.name, role = TextRole.HeadlineSmall, tone = TextTone.OnPrimaryContainer)
                DsText(activity.description, role = TextRole.BodyLarge, tone = TextTone.OnPrimaryContainer)
            }
        }
        ActivityBadges(activity, Spacing.Small)
    }
}

@Composable
private fun ActivityDetailSections(activity: Activity) {
    val uriHandler = LocalUriHandler.current
    Column(
        Modifier.padding(horizontal = Spacing.ExtraLarge),
        verticalArrangement = Arrangement.spacedBy(Spacing.LargePlus),
    ) {
        DsSection(title = "Comment jouer") {
            activity.steps.forEachIndexed { index, step -> DsNumberedItem(index + 1, step.text) }
        }
        if (activity.equipment.isNotEmpty()) {
            DsSection(title = "Matériel") {
                activity.equipment.forEach { item -> DsBulletItem(item.label) }
            }
        }
        activity.note?.let { note ->
            DsCallout(title = "À garder en tête", body = note, modifier = Modifier.fillMaxWidth())
        }
        if (activity.variants.isNotEmpty()) {
            DsSection(title = "Pour varier") {
                activity.variants.forEach { variant -> DsText(variant, role = TextRole.BodyLarge) }
            }
        }
        activity.document?.let { source ->
            DsTextButton("Voir la source : Naître et grandir", onClick = { uriHandler.openUri(source.value) })
        }
        Spacer(Modifier.height(Spacing.Medium))
    }
}

private const val BackLabel = "←  Retour aux fiches"
