package com.sotopatrick.onjoue

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.sotopatrick.onjoue.presentation.navigation.AppNavigationScaffold
import com.sotopatrick.onjoue.presentation.navigation.TopLevelDestination
import com.sotopatrick.onjoue.presentation.settings.SettingsScreen
import com.sotopatrick.onjoue.presentation.theme.OnJoueTheme

@Preview(widthDp = 400, heightDp = 400)
@Preview(widthDp = 400, heightDp = 500)
@Preview(widthDp = 400, heightDp = 1000)
@Preview(widthDp = 610, heightDp = 400)
@Preview(widthDp = 610, heightDp = 500)
@Preview(widthDp = 610, heightDp = 1000)
@Preview(widthDp = 900, heightDp = 400)
@Preview(widthDp = 900, heightDp = 500)
@Preview(widthDp = 900, heightDp = 1000)
annotation class NavigationSizes

@PreviewTest
@NavigationSizes
@Composable
fun SettingsNavigationPreview() {
    OnJoueTheme(darkTheme = false) { SettingsNavigationContent() }
}

@PreviewTest
@Preview(widthDp = 400, heightDp = 500)
@Composable
fun DarkSettingsNavigationPreview() {
    OnJoueTheme(darkTheme = true) { SettingsNavigationContent() }
}

@PreviewTest
@Preview(widthDp = 400, heightDp = 500, fontScale = 1.5f)
@Composable
fun LargeTextSettingsNavigationPreview() {
    OnJoueTheme(darkTheme = false) { SettingsNavigationContent() }
}

@Composable
private fun SettingsNavigationContent() {
    AppNavigationScaffold(TopLevelDestination.SETTINGS, onSelect = {}) {
        SettingsScreen(true, onManageTags = {}, onManageEquipment = {}, onManageActivities = {})
    }
}
