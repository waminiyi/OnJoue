package com.sotopatrick.onjoue.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sotopatrick.onjoue.presentation.common.UnavailableScreen
import com.sotopatrick.onjoue.presentation.management.ManagementCategory
import com.sotopatrick.onjoue.presentation.management.ManagementViewModel
import com.sotopatrick.onjoue.presentation.management.TagEquipmentManagementScreen
import com.sotopatrick.onjoue.presentation.settings.SettingsScreen

@Composable
fun AppNavigation(createManagement: (() -> ManagementViewModel)?) {
    val managementEnabled = createManagement != null
    val state = rememberAppNavigationState(managementEnabled)
    val navigator = remember(state, managementEnabled) { AppNavigator(state, managementEnabled) }
    val provider = entryProvider<NavKey> {
        entry<AppRoute.Idea> { UnavailableScreen("Idée") }
        entry<AppRoute.ActivitySheets> { UnavailableScreen("Fiches") }
        entry<AppRoute.Favorites> { UnavailableScreen("Favoris") }
        entry<AppRoute.Settings> {
            SettingsScreen(
                managementEnabled = managementEnabled,
                onManageTags = { navigator.openManagement(AppRoute.TagManagement) },
                onManageEquipment = { navigator.openManagement(AppRoute.EquipmentManagement) },
                onManageActivities = { navigator.openManagement(AppRoute.ActivityManagement) },
            )
        }
        if (createManagement != null) {
            entry<AppRoute.TagManagement> {
                ManagementEntry(ManagementCategory.TAGS, createManagement, navigator)
            }
            entry<AppRoute.EquipmentManagement> {
                ManagementEntry(ManagementCategory.EQUIPMENT, createManagement, navigator)
            }
            entry<AppRoute.ActivityManagement> {
                UnavailableScreen("Gérer les activités", onBack = navigator::goBack)
            }
        }
    }
    val entries = TopLevelDestination.entries.associateWith { destination ->
        rememberDecoratedNavEntries(
            backStack = state.backStacks.getValue(destination),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
                rememberViewModelStoreNavEntryDecorator<NavKey>(),
            ),
            entryProvider = provider,
        )
    }
    val visibleDestinations = if (state.selectedDestination == TopLevelDestination.IDEA) {
        listOf(TopLevelDestination.IDEA)
    } else {
        listOf(TopLevelDestination.IDEA, state.selectedDestination)
    }
    AppNavigationScaffold(state.selectedDestination, navigator::select) {
        NavDisplay(
            entries = visibleDestinations.flatMap { entries.getValue(it) },
            onBack = navigator::goBack,
        )
    }
}

@Composable
private fun ManagementEntry(
    category: ManagementCategory,
    createManagement: () -> ManagementViewModel,
    navigator: AppNavigator,
) {
    val vm = viewModel { createManagement() }
    val state by vm.state.collectAsStateWithLifecycle()
    val route = if (category == ManagementCategory.TAGS) AppRoute.TagManagement else AppRoute.EquipmentManagement
    DisposableEffect(navigator, route, state.busy) {
        navigator.setBackBlocked(route, state.busy)
        onDispose { navigator.setBackBlocked(route, false) }
    }
    TagEquipmentManagementScreen(vm, category, onBack = navigator::goBack)
}
