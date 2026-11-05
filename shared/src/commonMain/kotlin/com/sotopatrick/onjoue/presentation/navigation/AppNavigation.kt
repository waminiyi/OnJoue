package com.sotopatrick.onjoue.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sotopatrick.onjoue.presentation.common.UnavailableScreen
import com.sotopatrick.onjoue.presentation.activity.ActivitySheetsScreen
import com.sotopatrick.onjoue.presentation.activity.ActivitySheetsViewModel
import com.sotopatrick.onjoue.presentation.activity.ActivityDetailScreen
import com.sotopatrick.onjoue.presentation.activity.ActivityDetailsViewModel
import com.sotopatrick.onjoue.presentation.management.ManagementCategory
import com.sotopatrick.onjoue.presentation.management.ManagementViewModel
import com.sotopatrick.onjoue.presentation.management.TagEquipmentManagementScreen
import com.sotopatrick.onjoue.presentation.settings.CatalogResetViewModel
import com.sotopatrick.onjoue.presentation.settings.SettingsScreen

@Composable
fun AppNavigation(
    createManagement: (() -> ManagementViewModel)?,
    createActivitySheets: (() -> ActivitySheetsViewModel)? = null,
    createActivityDetails: ((String) -> ActivityDetailsViewModel)? = null,
    createCatalogReset: (() -> CatalogResetViewModel)? = null,
    modifier: Modifier = Modifier,
) {
    val managementEnabled = createManagement != null
    val state = rememberAppNavigationState(managementEnabled)
    val navigator = remember(state, managementEnabled) { AppNavigator(state, managementEnabled) }
    val provider = entryProvider<NavKey> {
        entry<AppRoute.Idea> { UnavailableScreen("Idée") }
        entry<AppRoute.ActivitySheets> { ActivitySheetsEntry(createActivitySheets, navigator) }
        entry<AppRoute.ActivityDetail> { route -> ActivityDetailEntry(route, createActivityDetails, navigator) }
        entry<AppRoute.Favorites> { UnavailableScreen("Favoris") }
        entry<AppRoute.Settings> { SettingsEntry(managementEnabled, createCatalogReset, navigator) }
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
    AppNavigationScaffold(state.selectedDestination, navigator::select, modifier) {
        NavDisplay(
            entries = visibleDestinations.flatMap { entries.getValue(it) },
            onBack = navigator::goBack,
        )
    }
}

@Composable
private fun ActivitySheetsEntry(create: (() -> ActivitySheetsViewModel)?, navigator: AppNavigator) {
    if (create == null) {
        UnavailableScreen("Fiches")
    } else {
        val vm = viewModel { create() }
        val state by vm.state.collectAsStateWithLifecycle()
        ActivitySheetsScreen(state, vm::load, onSelectActivity = navigator::openActivityDetail)
    }
}

@Composable
private fun ActivityDetailEntry(
    route: AppRoute.ActivityDetail,
    create: ((String) -> ActivityDetailsViewModel)?,
    navigator: AppNavigator,
) {
    if (create == null) {
        UnavailableScreen("Détail de la fiche", onBack = navigator::goBack)
    } else {
        val vm = viewModel { create(route.activityId) }
        val state by vm.state.collectAsStateWithLifecycle()
        ActivityDetailScreen(state, onBack = navigator::goBack, onRetry = vm::load)
    }
}

@Composable
private fun SettingsEntry(
    managementEnabled: Boolean,
    createCatalogReset: (() -> CatalogResetViewModel)?,
    navigator: AppNavigator,
) {
    val catalogResetViewModel = createCatalogReset?.let { create ->
        viewModel { create() }
    }
    val catalogResetState = catalogResetViewModel?.state?.collectAsStateWithLifecycle()?.value
    SettingsScreen(
        managementEnabled = managementEnabled,
        onManageTags = { navigator.openManagement(AppRoute.TagManagement) },
        onManageEquipment = { navigator.openManagement(AppRoute.EquipmentManagement) },
        onManageActivities = { navigator.openManagement(AppRoute.ActivityManagement) },
        catalogResetState = catalogResetState,
        onResetCatalog = catalogResetViewModel?.let { it::reset },
    )
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
