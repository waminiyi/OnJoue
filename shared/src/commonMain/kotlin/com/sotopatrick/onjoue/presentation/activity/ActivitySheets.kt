package com.sotopatrick.onjoue.presentation.activity

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import com.sotopatrick.onjoue.data.models.activity.Activity
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.designsystem.components.ActionTone
import com.sotopatrick.onjoue.designsystem.components.DsBadge
import com.sotopatrick.onjoue.designsystem.components.DsBulletItem
import com.sotopatrick.onjoue.designsystem.components.DsButton
import com.sotopatrick.onjoue.designsystem.components.DsCallout
import com.sotopatrick.onjoue.designsystem.components.DsHeroBanner
import com.sotopatrick.onjoue.designsystem.components.DsIllustration
import com.sotopatrick.onjoue.designsystem.components.DsLoadingState
import com.sotopatrick.onjoue.designsystem.components.DsNumberedItem
import com.sotopatrick.onjoue.designsystem.components.DsSection
import com.sotopatrick.onjoue.designsystem.components.DsText
import com.sotopatrick.onjoue.designsystem.components.DsTextButton
import com.sotopatrick.onjoue.designsystem.components.IllustrationSize
import com.sotopatrick.onjoue.designsystem.components.TextRole
import com.sotopatrick.onjoue.designsystem.components.TextTone
import com.sotopatrick.onjoue.designsystem.foundation.Spacing

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
            DsText(state.error?.content ?: "Cette idée n’est plus disponible.", tone = TextTone.Error)
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

private const val BackLabel = "←  Retour aux idées"
