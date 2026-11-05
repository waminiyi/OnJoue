package com.sotopatrick.onjoue.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.sotopatrick.onjoue.designsystem.tokens.AppShapes
import com.sotopatrick.onjoue.designsystem.tokens.AppTypography
import com.sotopatrick.onjoue.designsystem.tokens.DarkColors
import com.sotopatrick.onjoue.designsystem.tokens.LightColors

/** Root theme of OnJoue. Follows the system light/dark mode unless [darkTheme] is forced (previews, tests). */
@Composable
public fun OnJoueTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
