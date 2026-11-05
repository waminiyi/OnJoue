package com.sotopatrick.onjoue.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter

/**
 * Top-level navigation container: bottom bar or rail depending on the window size.
 * Content is kept inside the safe drawing area.
 */
@Composable
public fun DsNavigationScaffold(
    navigationItems: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    NavigationSuiteScaffold(
        modifier = modifier.fillMaxSize(),
        navigationItems = navigationItems,
    ) {
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            content()
        }
    }
}

/** Destination of a [DsNavigationScaffold], with an icon and a French label. */
@Composable
public fun DsNavigationItem(
    selected: Boolean,
    label: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationSuiteItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        label = {
            Text(
                label,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        modifier = modifier,
    )
}
