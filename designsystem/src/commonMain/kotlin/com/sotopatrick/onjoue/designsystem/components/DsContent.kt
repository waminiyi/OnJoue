package com.sotopatrick.onjoue.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.sotopatrick.onjoue.designsystem.foundation.Spacing
import com.sotopatrick.onjoue.designsystem.tokens.ComponentShapes
import com.sotopatrick.onjoue.designsystem.tokens.Dimens
import com.sotopatrick.onjoue.designsystem.tokens.HeroIllustrationSurfaceAlpha

/** Visual emphasis of a badge. */
public enum class BadgeTone {
    /** Activity attributes (age, duration, mood). */
    Accent,

    /** Neutral metadata (usage count, parent). */
    Neutral,
}

/** Pill-shaped single-line label. */
@Composable
public fun DsBadge(label: String, modifier: Modifier = Modifier, tone: BadgeTone = BadgeTone.Accent) {
    when (tone) {
        BadgeTone.Accent -> Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape,
        ) {
            DsText(
                label,
                Modifier.padding(horizontal = Spacing.SmallPlus, vertical = Spacing.XXSmallPlus),
                role = TextRole.LabelMedium,
                tone = TextTone.OnPrimaryContainer,
                maxLines = 1,
            )
        }
        BadgeTone.Neutral -> Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            DsText(
                label,
                Modifier.padding(horizontal = Spacing.SmallPlus, vertical = Spacing.XXSmall),
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
}

/** Emoji illustration on a tinted rounded background. */
@Composable
public fun DsIllustration(
    emoji: String,
    modifier: Modifier = Modifier,
    size: IllustrationSize = IllustrationSize.Medium,
) {
    val background = when (size) {
        IllustrationSize.Medium -> MaterialTheme.colorScheme.primaryContainer
        IllustrationSize.Large -> MaterialTheme.colorScheme.surface.copy(alpha = HeroIllustrationSurfaceAlpha)
    }
    val medium = size == IllustrationSize.Medium
    Box(
        modifier
            .size(if (medium) Dimens.IllustrationSize else Dimens.HeroIllustrationSize)
            .clip(if (medium) ComponentShapes.Illustration else ComponentShapes.HeroIllustration)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        DsText(
            emoji,
            role = if (size == IllustrationSize.Medium) TextRole.HeadlineMedium else TextRole.HeadlineLarge,
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
public fun DsBulletItem(text: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        DsText("•", tone = TextTone.Primary)
        Spacer(Modifier.width(Spacing.Small))
        DsText(text, role = TextRole.BodyLarge)
    }
}
