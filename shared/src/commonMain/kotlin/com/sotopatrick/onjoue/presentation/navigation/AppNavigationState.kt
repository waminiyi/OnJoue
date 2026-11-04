package com.sotopatrick.onjoue.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

class AppNavigationState(
    selectedDestination: MutableState<TopLevelDestination>,
    val backStacks: Map<TopLevelDestination, MutableList<NavKey>>,
) {
    var selectedDestination by selectedDestination
        internal set

    val currentStack: MutableList<NavKey>
        get() = backStacks.getValue(selectedDestination)

    val canGoBack: Boolean
        get() = currentStack.size > 1 || selectedDestination != TopLevelDestination.IDEA

    fun removeManagementRoutes() {
        backStacks.values.forEach { stack ->
            stack.removeAll {
                it == AppRoute.TagManagement || it == AppRoute.EquipmentManagement ||
                    it == AppRoute.ActivityManagement
            }
        }
    }
}

@Composable
fun rememberAppNavigationState(managementEnabled: Boolean): AppNavigationState {
    val selected = rememberSaveable(
        stateSaver = Saver<TopLevelDestination, String>(
            save = { it.name },
            restore = { TopLevelDestination.valueOf(it) },
        ),
    ) { mutableStateOf(TopLevelDestination.IDEA) }
    val stacks = TopLevelDestination.entries.associateWith {
        rememberNavBackStack(NavigationSavedStateConfiguration, it.route)
    }
    val state = remember { AppNavigationState(selected, stacks) }
    if (!managementEnabled) state.removeManagementRoutes()
    return state
}
