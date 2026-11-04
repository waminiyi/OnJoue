package com.sotopatrick.onjoue.presentation.navigation

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
import androidx.compose.ui.platform.testTag
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
    NavigationItem(TopLevelDestination.IDEA, "Idée", Res.drawable.ic_idea),
    NavigationItem(TopLevelDestination.ACTIVITY_SHEETS, "Fiches", Res.drawable.ic_sheets),
    NavigationItem(TopLevelDestination.FAVORITES, "Favoris", Res.drawable.ic_favorites),
    NavigationItem(TopLevelDestination.SETTINGS, "Paramètres", Res.drawable.ic_settings),
)

@Composable
fun AppNavigationScaffold(
    selectedDestination: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit,
    content: @Composable () -> Unit,
) {
    NavigationSuiteScaffold(
        modifier = Modifier.fillMaxSize(),
        navigationItems = {
            navigationItems.forEach { item ->
                NavigationSuiteItem(
                    selected = selectedDestination == item.destination,
                    onClick = { onSelect(item.destination) },
                    icon = { Icon(painterResource(item.icon), contentDescription = null) },
                    label = {
                        Text(
                            item.label,
                            color = if (selectedDestination == item.destination) {
                                MaterialTheme.colorScheme.primary
                            } else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    modifier = Modifier.testTag("navigation-${item.destination.name}"),
                )
            }
        },
    ) {
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            content()
        }
    }
}
