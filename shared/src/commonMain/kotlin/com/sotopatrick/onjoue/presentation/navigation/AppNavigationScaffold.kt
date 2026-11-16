package com.sotopatrick.onjoue.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.sotopatrick.onjoue.designsystem.components.DsNavigationItem
import com.sotopatrick.onjoue.designsystem.components.DsNavigationScaffold
import onjoue.shared.generated.resources.Res
import onjoue.shared.generated.resources.ic_favorites
import onjoue.shared.generated.resources.ic_idea
import onjoue.shared.generated.resources.ic_settings
import onjoue.shared.generated.resources.ic_sheets
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private data class NavigationItem(
    val destination: TopLevelDestination,
    val label: String,
    val icon: DrawableResource,
)

private val navigationItems = listOf(
    NavigationItem(TopLevelDestination.IDEA, "Aujourd’hui", Res.drawable.ic_idea),
    NavigationItem(TopLevelDestination.ACTIVITY_SHEETS, "Idées", Res.drawable.ic_sheets),
    NavigationItem(TopLevelDestination.FAVORITES, "Favoris", Res.drawable.ic_favorites),
    NavigationItem(TopLevelDestination.SETTINGS, "Paramètres", Res.drawable.ic_settings),
)

@Composable
fun AppNavigationScaffold(
    selectedDestination: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    DsNavigationScaffold(
        modifier = modifier,
        navigationItems = {
            navigationItems.forEach { item ->
                DsNavigationItem(
                    selected = selectedDestination == item.destination,
                    label = item.label,
                    icon = painterResource(item.icon),
                    onClick = { onSelect(item.destination) },
                    modifier = Modifier.testTag("navigation-${item.destination.name}"),
                )
            }
        },
        content = content,
    )
}
