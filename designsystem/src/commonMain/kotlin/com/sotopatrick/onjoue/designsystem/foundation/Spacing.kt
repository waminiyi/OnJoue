package com.sotopatrick.onjoue.designsystem.foundation

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale of OnJoue, used with the standard Compose APIs (`Modifier.padding`, `Arrangement.spacedBy`, `Spacer`).
 * The usual steps are [Small], [Medium], [Large], [ExtraLarge] and [XXLarge]; the "Plus" steps are screen-specific
 * rhythms kept for existing layouts. Use the steps as-is: arithmetic on them is rejected by Detekt.
 */
public object Spacing {
    public val None: Dp = 0.dp
    public val XXSmall: Dp = 4.dp
    public val XXSmallPlus: Dp = 5.dp
    public val ExtraSmall: Dp = 6.dp
    public val Small: Dp = 8.dp
    public val SmallPlus: Dp = 10.dp
    public val Medium: Dp = 12.dp
    public val MediumPlus: Dp = 14.dp
    public val Large: Dp = 16.dp
    public val LargePlus: Dp = 18.dp
    public val ExtraLarge: Dp = 20.dp
    public val XXLarge: Dp = 24.dp
}
