package com.sotopatrick.onjoue.designsystem.foundation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Minimum available width (scaled by the font scale) to switch a block to its wide layout. */
public enum class Breakpoint(internal val minWidth: Dp) {
    /** Card content next to its actions. */
    Compact(240.dp),

    /** Card content with a leading monogram next to its actions. */
    CompactPlus(280.dp),

    /** Two form fields side by side. */
    Medium(340.dp),
}

/**
 * Measures the available width and tells [content] whether the wide layout fits.
 * The threshold grows with large font scales so text never gets squeezed.
 */
@Composable
public fun DsWidthBreakpoint(
    breakpoint: Breakpoint,
    modifier: Modifier = Modifier,
    content: @Composable (isWide: Boolean) -> Unit,
) {
    BoxWithConstraints(modifier) {
        val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
        content(maxWidth >= breakpoint.minWidth * fontScale)
    }
}
