package com.sotopatrick.onjoue.designsystem.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

/** Typographic role of a text, mapped to the theme typography. [Inherit] keeps the surrounding style. */
public enum class TextRole {
    Inherit,
    HeadlineLarge,
    HeadlineMedium,
    HeadlineSmall,
    TitleLarge,
    TitleMedium,
    TitleSmall,
    BodyLarge,
    BodyMedium,
    BodySmall,
    LabelLarge,
    LabelMedium,
}

/** Semantic color of a text. [Inherit] keeps the content color of the surrounding component. */
public enum class TextTone {
    Inherit,
    Default,
    Secondary,
    Primary,
    Error,
    OnPrimaryContainer,
}

@Composable
public fun DsText(
    text: String,
    modifier: Modifier = Modifier,
    role: TextRole = TextRole.Inherit,
    tone: TextTone = TextTone.Inherit,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        color = tone.color(),
        style = role.style(),
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
@ReadOnlyComposable
internal fun TextRole.style(): TextStyle {
    val typography = MaterialTheme.typography
    return when (this) {
        TextRole.Inherit -> LocalTextStyle.current
        TextRole.HeadlineLarge -> typography.headlineLarge
        TextRole.HeadlineMedium -> typography.headlineMedium
        TextRole.HeadlineSmall -> typography.headlineSmall
        TextRole.TitleLarge -> typography.titleLarge
        TextRole.TitleMedium -> typography.titleMedium
        TextRole.TitleSmall -> typography.titleSmall
        TextRole.BodyLarge -> typography.bodyLarge
        TextRole.BodyMedium -> typography.bodyMedium
        TextRole.BodySmall -> typography.bodySmall
        TextRole.LabelLarge -> typography.labelLarge
        TextRole.LabelMedium -> typography.labelMedium
    }
}

@Composable
@ReadOnlyComposable
internal fun TextTone.color(): Color {
    val colors = MaterialTheme.colorScheme
    return when (this) {
        TextTone.Inherit -> Color.Unspecified
        TextTone.Default -> colors.onSurface
        TextTone.Secondary -> colors.onSurfaceVariant
        TextTone.Primary -> colors.primary
        TextTone.Error -> colors.error
        TextTone.OnPrimaryContainer -> colors.onPrimaryContainer
    }
}
