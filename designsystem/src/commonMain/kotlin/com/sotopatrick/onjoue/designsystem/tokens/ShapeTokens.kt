package com.sotopatrick.onjoue.designsystem.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

internal val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Screen-specific radii kept out of the Material shape scale. */
internal object ComponentShapes {
    val SearchField = RoundedCornerShape(18.dp)
    val Illustration = RoundedCornerShape(20.dp)
    val HeroIllustration = RoundedCornerShape(24.dp)
    val Callout = RoundedCornerShape(20.dp)
}
