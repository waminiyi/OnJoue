package com.sotopatrick.onjoue.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B7B72),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDF3EF),
    onPrimaryContainer = Color(0xFF0B7B72),
    secondary = Color(0xFF64798B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDF3EF),
    onSecondaryContainer = Color(0xFF0B7B72),
    background = Color(0xFFF4F6F8),
    onBackground = Color(0xFF13232D),
    surface = Color.White,
    onSurface = Color(0xFF13232D),
    surfaceVariant = Color(0xFFF4F6F8),
    onSurfaceVariant = Color(0xFF64798B),
    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color(0xFFF4F6F8),
    surfaceContainerHighest = Color(0xFFEAF0F3),
    outline = Color(0xFF64798B),
    outlineVariant = Color(0xFFDFE7ED),
    error = Color(0xFFBD3700),
    onError = Color.White,
    errorContainer = Color(0xFFFFE5DA),
    onErrorContainer = Color(0xFF8B2800),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF80D5C9),
    onPrimary = Color(0xFF003C36),
    primaryContainer = Color(0xFF174B45),
    onPrimaryContainer = Color(0xFFA5EDE2),
    secondary = Color(0xFFA9BDCD),
    secondaryContainer = Color(0xFF174B45),
    onSecondaryContainer = Color(0xFFA5EDE2),
    background = Color(0xFF0F1A20),
    onBackground = Color(0xFFE3EEF3),
    surface = Color(0xFF18282F),
    onSurface = Color(0xFFE3EEF3),
    surfaceVariant = Color(0xFF24363F),
    onSurfaceVariant = Color(0xFFA9BDCD),
    surfaceContainer = Color(0xFF18282F),
    surfaceContainerLow = Color(0xFF18282F),
    surfaceContainerHigh = Color(0xFF24363F),
    surfaceContainerHighest = Color(0xFF30434D),
    outline = Color(0xFFA9BDCD),
    outlineVariant = Color(0xFF344852),
    error = Color(0xFFFFB69A),
    onError = Color(0xFF561800),
    errorContainer = Color(0xFF783014),
    onErrorContainer = Color(0xFFFFDBCB),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private val BaseTypography = Typography()
private val AppTypography = BaseTypography.copy(
    headlineLarge = BaseTypography.headlineLarge.copy(fontWeight = FontWeight.Bold),
    headlineMedium = BaseTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    headlineSmall = BaseTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLarge = BaseTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = BaseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = BaseTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    labelMedium = BaseTypography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
)

@Composable
fun OnJoueTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
