package com.sotopatrick.onjoue.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import com.sotopatrick.onjoue.designsystem.foundation.Spacing
import com.sotopatrick.onjoue.designsystem.tokens.Dimens

/** Semantic color of a secondary action. A color never carries the meaning alone: keep an explicit label. */
public enum class ActionTone {
    Default,
    Destructive,
    OnPrimaryContainer,
    Tertiary,
}

/** Main filled action of a screen or dialog. */
@Composable
public fun DsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tone: ActionTone = ActionTone.Default,
) {
    val colors = when (tone) {
        ActionTone.OnPrimaryContainer -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        ActionTone.Tertiary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
        )
        ActionTone.Destructive -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        )
        else -> ButtonDefaults.buttonColors()
    }
    Button(onClick = onClick, modifier = modifier, enabled = enabled, colors = colors) { Text(text) }
}

/** Low-emphasis action (back, retry, cancel, links). */
@Composable
public fun DsTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tone: ActionTone = ActionTone.Default,
) {
    val contentColor = tone.contentColor()
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = if (contentColor == Color.Unspecified) {
            ButtonDefaults.textButtonColors()
        } else {
            ButtonDefaults.textButtonColors(contentColor = contentColor)
        },
    ) { Text(text) }
}

/** Square 48 dp outlined action with an icon and a mandatory accessible description. */
@Composable
public fun DsOutlinedIconButton(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tone: ActionTone = ActionTone.Default,
) {
    val contentColor = tone.contentColor().takeIf { it != Color.Unspecified } ?: MaterialTheme.colorScheme.onSurface
    OutlinedButton(
        enabled = enabled,
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(Dimens.BorderWidth, MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
        contentPadding = PaddingValues(Spacing.Medium),
        modifier = modifier.size(Dimens.TouchTarget),
    ) { Icon(icon, contentDescription = contentDescription) }
}

/** Primary floating action, e.g. creation. */
@Composable
public fun DsFloatingActionButton(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) { Icon(icon, contentDescription = contentDescription) }
}

@Composable
public fun DsFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) }, modifier = modifier)
}

/** Filter icon chip matching [DsFilterChip] height, displaying an icon and an optional active count badge. */
@Composable
public fun DsFilterIconButton(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeCount: Int = 0,
    enabled: Boolean = true,
) {
    FilterChip(
        selected = activeCount > 0,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        leadingIcon = { Icon(icon, contentDescription = contentDescription) },
        label = {
            if (activeCount > 0) {
                DsText("$activeCount", role = TextRole.LabelMedium)
            }
        },
    )
}


/** Transparent toggle action with an explicit accessible state and label. */
@Composable
public fun DsIconToggleButton(
    checked: Boolean,
    icon: Painter,
    contentDescription: String,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    IconToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
    ) { Icon(icon, contentDescription = contentDescription) }
}

/** Indeterminate progress for loading and busy states. */
@Composable
public fun DsProgressIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier)
}

@Composable
@ReadOnlyComposable
private fun ActionTone.contentColor(): Color = when (this) {
    ActionTone.Default -> Color.Unspecified
    ActionTone.Destructive -> MaterialTheme.colorScheme.error
    ActionTone.OnPrimaryContainer -> MaterialTheme.colorScheme.onPrimaryContainer
    ActionTone.Tertiary -> MaterialTheme.colorScheme.tertiary
}
