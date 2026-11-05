package com.sotopatrick.onjoue.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sotopatrick.onjoue.designsystem.foundation.Spacing
import com.sotopatrick.onjoue.designsystem.tokens.ComponentShapes
import com.sotopatrick.onjoue.designsystem.tokens.Dimens

/** Elevation of a content card. */
public enum class CardElevation {
    /** Dense lists of browsable cards. */
    Low,

    /** Standalone action or management cards. */
    Raised,
}

/** Content card: surface, discreet border and large radius. Clickable when [onClick] is provided. */
@Composable
public fun DsCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: CardElevation = CardElevation.Raised,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val border = BorderStroke(Dimens.BorderWidth, MaterialTheme.colorScheme.outlineVariant)
    val cardElevation = CardDefaults.cardElevation(
        defaultElevation = when (elevation) {
            CardElevation.Low -> Dimens.ElevationLow
            CardElevation.Raised -> Dimens.ElevationRaised
        },
    )
    if (onClick == null) {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = cardElevation,
            border = border,
            content = content,
        )
    } else {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = cardElevation,
            border = border,
            content = content,
        )
    }
}

/** Card describing an action with a title and a description, e.g. in settings. */
@Composable
public fun DsActionCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DsCard(modifier = modifier, onClick = onClick) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { Text(description) },
        )
    }
}

/** Highlighted note, e.g. "À garder en tête" on an activity. */
@Composable
public fun DsCallout(title: String, body: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = ComponentShapes.Callout,
    ) {
        Column(
            Modifier.padding(Spacing.Large),
            verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
        ) {
            DsText(title, role = TextRole.TitleSmall)
            DsText(body, role = TextRole.BodyMedium)
        }
    }
}

/** Tinted header block at the top of a detail screen. Text inside uses [TextTone.OnPrimaryContainer]. */
@Composable
public fun DsHeroBanner(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = Dimens.HeroHorizontalPadding, vertical = Spacing.LargePlus),
        verticalArrangement = Arrangement.spacedBy(Spacing.MediumPlus),
        content = content,
    )
}

/** Titled block of a detail screen. */
@Composable
public fun DsSection(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.Medium)) {
        DsText(title, role = TextRole.TitleLarge)
        content()
    }
}

/** Screen title with an optional secondary subtitle. */
@Composable
public fun DsScreenHeader(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.XXSmallPlus)) {
        DsText(title, role = TextRole.HeadlineMedium)
        subtitle?.let { DsText(it, role = TextRole.BodyLarge, tone = TextTone.Secondary) }
    }
}

/** Background surface for full-screen states displayed outside of the navigation (startup…). */
@Composable
public fun DsBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(modifier, color = MaterialTheme.colorScheme.background, content = content)
}

/** Full-size centered progress indicator. */
@Composable
public fun DsLoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { DsProgressIndicator() }
}

/** Full-width error message followed by a retry action. */
@Composable
public fun DsErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryLabel: String = "Réessayer",
) {
    Column(
        modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        DsText(message, tone = TextTone.Error)
        DsButton(retryLabel, onClick = onRetry)
    }
}
