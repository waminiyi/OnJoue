package com.sotopatrick.onjoue.presentation.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
    fun switchingAndReselectingPreservesEachStack() {
        val state = state()
        val navigator = AppNavigator(state, managementEnabled = true)
        navigator.openManagement(AppRoute.TagManagement)
        navigator.select(TopLevelDestination.FAVORITES)
        navigator.select(TopLevelDestination.SETTINGS)
        navigator.select(TopLevelDestination.SETTINGS)
        navigator.openManagement(AppRoute.TagManagement)
        assertEquals<List<NavKey>>(listOf(AppRoute.Settings, AppRoute.TagManagement), state.currentStack)
        assertEquals<List<NavKey>>(listOf(AppRoute.Favorites), state.backStacks.getValue(TopLevelDestination.FAVORITES))
    }

    @Test
    fun backReturnsToSettingsThenIdeaWithoutClearingOtherTabs() {
        val state = state()
        val navigator = AppNavigator(state, managementEnabled = true)
        navigator.openManagement(AppRoute.EquipmentManagement)
        assertTrue(state.canGoBack)
        navigator.goBack()
        assertEquals<List<NavKey>>(listOf(AppRoute.Settings), state.currentStack)
        navigator.goBack()
        assertEquals(TopLevelDestination.IDEA, state.selectedDestination)
        assertFalse(state.canGoBack)
        navigator.goBack()
        assertEquals<List<NavKey>>(listOf(AppRoute.Idea), state.currentStack)
    }

    @Test
    fun allManagementRoutesAreUnavailableInRelease() {
        val state = state()
        val navigator = AppNavigator(state, managementEnabled = false)
        listOf(AppRoute.TagManagement, AppRoute.EquipmentManagement, AppRoute.ActivityManagement).forEach {
            assertFailsWith<IllegalStateException> { navigator.openManagement(it) }
        }
        assertEquals(TopLevelDestination.IDEA, state.selectedDestination)
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
    fun busyEntryCannotBePoppedButDoesNotBlockAnotherTab() {
        val state = state()
        val navigator = AppNavigator(state, managementEnabled = true)
        navigator.openManagement(AppRoute.TagManagement)
        navigator.setBackBlocked(AppRoute.TagManagement, true)
        navigator.goBack()
        assertEquals(AppRoute.TagManagement, state.currentStack.last())
        navigator.select(TopLevelDestination.FAVORITES)
        navigator.goBack()
        assertEquals(TopLevelDestination.IDEA, state.selectedDestination)
        navigator.select(TopLevelDestination.SETTINGS)
        navigator.setBackBlocked(AppRoute.TagManagement, false)
        navigator.goBack()
        assertEquals(AppRoute.Settings, state.currentStack.last())
    }

    @Test
    fun everyRouteRoundTripsWithTheMultiplatformPolymorphicSerializer() {
        val json = Json { serializersModule = NavigationSavedStateConfiguration.serializersModule }
        val serializer = PolymorphicSerializer(NavKey::class)
        val routes = TopLevelDestination.entries.map { it.route } +
            listOf(AppRoute.TagManagement, AppRoute.EquipmentManagement, AppRoute.ActivityManagement)
        routes.forEach { route ->
            assertEquals(route, json.decodeFromString(serializer, json.encodeToString(serializer, route)))
        }
    }
}
