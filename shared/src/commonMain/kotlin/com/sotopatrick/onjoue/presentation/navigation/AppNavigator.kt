package com.sotopatrick.onjoue.presentation.navigation

class AppNavigator(
    val state: AppNavigationState,
    private val managementEnabled: Boolean,
) {
    private val blockedRoutes = mutableSetOf<AppRoute>()

    fun setBackBlocked(route: AppRoute, blocked: Boolean) {
        if (blocked) blockedRoutes.add(route) else blockedRoutes.remove(route)
    }

    fun select(destination: TopLevelDestination) {
        state.selectedDestination = destination
    }

    fun openManagement(route: AppRoute) {
        require(
            route == AppRoute.TagManagement || route == AppRoute.EquipmentManagement ||
                route == AppRoute.ActivityManagement,
        ) { "Expected a management route" }
        check(managementEnabled) { "Management is unavailable in this build" }
        state.selectedDestination = TopLevelDestination.SETTINGS
        if (state.currentStack.last() == AppRoute.Settings) state.currentStack.add(route)
    }

    fun goBack() {
        if (state.currentStack.last() in blockedRoutes) return
        if (state.currentStack.size > 1) {
            state.currentStack.removeAt(state.currentStack.lastIndex)
        } else if (state.selectedDestination != TopLevelDestination.IDEA) {
            state.selectedDestination = TopLevelDestination.IDEA
        }
    }
}
