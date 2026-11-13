package com.sotopatrick.onjoue.presentation.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json

class AppNavigatorTest {
    private fun state() = AppNavigationState(
        mutableStateOf(TopLevelDestination.IDEA),
        TopLevelDestination.entries.associateWith { mutableListOf<NavKey>(it.route) },
    )

    @Test
    fun tabsHaveTheExpectedOrderAndInitialDestination() {
        assertEquals(
            listOf(AppRoute.Idea, AppRoute.ActivitySheets, AppRoute.Favorites, AppRoute.Settings),
            TopLevelDestination.entries.map { it.route },
        )
        assertEquals(TopLevelDestination.IDEA, state().selectedDestination)
        assertFalse(state().canGoBack)
    }

    @Test
    fun switchingTabsPreservesActivityDetailAndBackReturnsToList() {
        val state = state()
        val navigator = AppNavigator(state)
        navigator.select(TopLevelDestination.ACTIVITY_SHEETS)
        navigator.openActivityDetail("activity-1")
        navigator.select(TopLevelDestination.FAVORITES)
        navigator.select(TopLevelDestination.ACTIVITY_SHEETS)
        assertEquals(AppRoute.ActivityDetail("activity-1"), state.currentStack.last())
        assertTrue(state.canGoBack)
        navigator.goBack()
        assertEquals<List<NavKey>>(listOf(AppRoute.ActivitySheets), state.currentStack)
        navigator.goBack()
        assertEquals(TopLevelDestination.IDEA, state.selectedDestination)
        assertFalse(state.canGoBack)
    }

    @Test
    fun restoredManagementRoutesAreRemovedWithoutRemovingSettings() {
        val state = state()
        state.backStacks.getValue(TopLevelDestination.SETTINGS).addAll(
            listOf(AppRoute.TagManagement, AppRoute.EquipmentManagement, AppRoute.ActivityManagement),
        )
        state.removeManagementRoutes()
        assertEquals<List<NavKey>>(listOf(AppRoute.Settings), state.backStacks.getValue(TopLevelDestination.SETTINGS))
    }

    @Test
    fun currentAndLegacyRoutesRoundTripForStateRestoration() {
        val json = Json { serializersModule = NavigationSavedStateConfiguration.serializersModule }
        val serializer = PolymorphicSerializer(NavKey::class)
        val routes = TopLevelDestination.entries.map { it.route } +
            listOf(
                AppRoute.TagManagement,
                AppRoute.EquipmentManagement,
                AppRoute.ActivityManagement,
                AppRoute.ActivityDetail("activity-1"),
            )
        routes.forEach { route ->
            assertEquals(route, json.decodeFromString(serializer, json.encodeToString(serializer, route)))
        }
    }
}
