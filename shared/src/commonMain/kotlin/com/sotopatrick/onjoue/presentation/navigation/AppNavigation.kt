package com.sotopatrick.onjoue.presentation.navigation

import androidx.compose.runtime.Composable
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
import com.sotopatrick.onjoue.presentation.activity.details.ActivityDetailScreen
import com.sotopatrick.onjoue.presentation.activity.details.ActivityDetailsViewModel
import com.sotopatrick.onjoue.presentation.activity.listing.ActivitySheetsScreen
import com.sotopatrick.onjoue.presentation.activity.listing.ActivitySheetsViewModel
import com.sotopatrick.onjoue.presentation.settings.SettingsScreen

@Composable
fun AppNavigation(
    createActivitySheets: (() -> ActivitySheetsViewModel)? = null,
    createActivityDetails: ((String) -> ActivityDetailsViewModel)? = null,
    modifier: Modifier = Modifier,
) {
    val state = rememberAppNavigationState()
    val navigator = remember(state) { AppNavigator(state) }
    val provider = entryProvider<NavKey> {
        entry<AppRoute.Idea> { UnavailableScreen("Aujourd’hui") }
        entry<AppRoute.ActivitySheets> { ActivitySheetsEntry(createActivitySheets, navigator) }
        entry<AppRoute.ActivityDetail> { route -> ActivityDetailEntry(route, createActivityDetails, navigator) }
        entry<AppRoute.Favorites> { UnavailableScreen("Favoris") }
        entry<AppRoute.Settings> { SettingsScreen() }
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
        UnavailableScreen("Idées")
    } else {
        val vm = viewModel { create() }
        val state by vm.state.collectAsStateWithLifecycle()
        ActivitySheetsScreen(
            state, vm::load,
            onSelectActivity = navigator::openActivityDetail,
            onToggleFavorite = vm::toggleFavorite,
            onRetryFavorites = vm::loadFavorites,
            onClearNotice = vm::clearNotice,
        )
    }
}

@Composable
private fun ActivityDetailEntry(
    route: AppRoute.ActivityDetail,
    create: ((String) -> ActivityDetailsViewModel)?,
    navigator: AppNavigator,
) {
    if (create == null) {
        UnavailableScreen("Détail de l’idée", onBack = navigator::goBack)
    } else {
        val vm = viewModel { create(route.activityId) }
        val state by vm.state.collectAsStateWithLifecycle()
        ActivityDetailScreen(
            state = state,
            onBack = navigator::goBack,
            onRetry = vm::load,
            onToggleFavorite = vm::toggleFavorite,
            onToggleTriedStatus = vm::toggleTried,
            onClearNotice = vm::clearNotice,
        )
    }
}
