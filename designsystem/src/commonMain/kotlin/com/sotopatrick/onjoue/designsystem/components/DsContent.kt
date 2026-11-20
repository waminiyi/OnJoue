package com.sotopatrick.onjoue.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import com.sotopatrick.onjoue.designsystem.foundation.Spacing
import com.sotopatrick.onjoue.designsystem.tokens.ComponentShapes
import com.sotopatrick.onjoue.designsystem.tokens.Dimens
import com.sotopatrick.onjoue.designsystem.tokens.HeroIllustrationSurfaceAlpha

/** Visual emphasis of a badge. */
public enum class BadgeTone {
    /** Activity attributes (age, duration, mood). */
    Accent,

    /** Surface-colored metadata, white in the light theme. */
    Surface,

    /** Neutral metadata (usage count, parent). */
    Neutral,

    /** Plain metadata without a background. */
    Plain,
}

/** Pill-shaped single-line label. */
@Composable
public fun DsBadge(
    label: String,
    modifier: Modifier = Modifier,
    tone: BadgeTone = BadgeTone.Accent,
    icon: Painter? = null,
) {
    when (tone) {
        BadgeTone.Plain -> DsPlainBadge(label, modifier, icon)
        BadgeTone.Accent -> DsAccentBadge(label, modifier, icon)
        BadgeTone.Surface -> DsSurfaceBadge(label, modifier, icon)
        BadgeTone.Neutral -> DsNeutralBadge(label, modifier, icon)
    }
}

@Composable
private fun DsPlainBadge(label: String, modifier: Modifier = Modifier, icon: Painter? = null) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.XXSmall),
    ) {
        icon?.let { DsIcon(it, contentDescription = null, modifier = Modifier.size(Dimens.IconSmall)) }
        DsText(label, role = TextRole.LabelMedium, tone = TextTone.Secondary)
    }
}

@Composable
private fun DsAccentBadge(label: String, modifier: Modifier = Modifier, icon: Painter? = null) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = CircleShape,
    ) {
        Row(
            Modifier.padding(horizontal = Spacing.SmallPlus, vertical = Spacing.XXSmallPlus),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.XXSmall),
        ) {
            icon?.let {
                DsIcon(
                    it,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.IconSmall),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            DsText(
                label,
                role = TextRole.LabelMedium,
                tone = TextTone.OnPrimaryContainer,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DsSurfaceBadge(label: String, modifier: Modifier = Modifier, icon: Painter? = null) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            Modifier.padding(horizontal = Spacing.SmallPlus, vertical = Spacing.XXSmallPlus),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.XXSmall),
        ) {
            icon?.let {
                DsIcon(
                    it,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.IconSmall),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DsText(label, role = TextRole.LabelMedium, maxLines = 1)
        }
    }
}

@Composable
private fun DsNeutralBadge(label: String, modifier: Modifier = Modifier, icon: Painter? = null) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            Modifier.padding(horizontal = Spacing.SmallPlus, vertical = Spacing.XXSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.XXSmall),
        ) {
            icon?.let {
                DsIcon(
                    it,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.IconSmall),
                )
            }
            DsText(
                label,
                role = TextRole.LabelLarge,
            )
        }
    }
}

/** Size of an emoji illustration. */
public enum class IllustrationSize {
    /** In a list card. */
    Medium,

    /** In a hero banner. */
    Large,

    /** Large centerpiece for a detail-screen hero. */
    ExtraLarge,
}

public enum class IllustrationStyle {
    Tinted,
    Plain,
}

/** Emoji illustration on a tinted rounded background. */
@Composable
public fun DsIllustration(
    emoji: String,
    modifier: Modifier = Modifier,
    size: IllustrationSize = IllustrationSize.Medium,
    style: IllustrationStyle = IllustrationStyle.Tinted,
) {
    val background = when (size) {
        IllustrationSize.Medium -> MaterialTheme.colorScheme.primaryContainer
        IllustrationSize.Large,
        IllustrationSize.ExtraLarge -> MaterialTheme.colorScheme.surface.copy(alpha = HeroIllustrationSurfaceAlpha)
    }
    val medium = size == IllustrationSize.Medium
    Box(
        modifier
            .size(size.dimension())
            .clip(if (medium) ComponentShapes.Illustration else ComponentShapes.HeroIllustration)
            .then(if (style == IllustrationStyle.Tinted) Modifier.background(background) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        DsText(
            emoji,
            role = when (size) {
                IllustrationSize.Medium -> TextRole.HeadlineMedium
                IllustrationSize.Large -> TextRole.HeadlineLarge
                IllustrationSize.ExtraLarge -> TextRole.HeadlineLarge
            },
        )
    }
}

/** Vector icon illustration on a tinted rounded background. */
@Composable
public fun DsVectorIllustration(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: IllustrationSize = IllustrationSize.Medium,
    style: IllustrationStyle = IllustrationStyle.Tinted,
) {
    val background = when (size) {
        IllustrationSize.Medium -> MaterialTheme.colorScheme.primaryContainer
        IllustrationSize.Large,
        IllustrationSize.ExtraLarge -> MaterialTheme.colorScheme.surface.copy(
            alpha = HeroIllustrationSurfaceAlpha,
        )
    }
    val medium = size == IllustrationSize.Medium
    val boxSize = size.dimension()
    val shape = if (medium) ComponentShapes.Illustration else ComponentShapes.HeroIllustration
    Box(
        modifier
            .size(boxSize)
            .clip(shape)
            .then(if (style == IllustrationStyle.Tinted) Modifier.background(background) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.size(boxSize / 2),
        )
    }
}

/** Initial letter on a tinted square, used as a lightweight avatar. */
@Composable
public fun DsMonogram(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        DsText(text, Modifier.padding(Spacing.Medium), role = TextRole.TitleLarge)
    }
}

/** Numbered step of a procedure. */
@Composable
public fun DsNumberedItem(number: Int, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            Modifier.size(Dimens.StepNumberSize).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            DsText("$number", role = TextRole.LabelLarge, tone = TextTone.OnPrimaryContainer)
        }
        DsText(text, Modifier.weight(1f), role = TextRole.BodyLarge)
    }
}

/** Bulleted list entry. */
@Composable
public fun DsBulletItem(
    text: String,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            androidx.compose.foundation.Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(Dimens.MaterialIconSize),
            )
        } else {
            DsText("•", tone = TextTone.Primary)
        }
        Spacer(Modifier.width(Spacing.Small))
        DsText(text, role = TextRole.BodyLarge)
    }
}
