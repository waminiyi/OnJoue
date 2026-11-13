package com.sotopatrick.onjoue.presentation.navigation

class AppNavigator(val state: AppNavigationState) {

    fun select(destination: TopLevelDestination) {
        state.selectedDestination = destination
    }

    fun openActivityDetail(activityId: String) {
        require(activityId.isNotBlank()) { "Expected a non-blank activity id" }
        check(state.selectedDestination == TopLevelDestination.ACTIVITY_SHEETS) {
            "Activity details can only be opened from the activity sheets tab"
        }
        state.currentStack.add(AppRoute.ActivityDetail(activityId))
    }

    fun goBack() {
        if (state.currentStack.size > 1) {
            state.currentStack.removeAt(state.currentStack.lastIndex)
        } else if (state.selectedDestination != TopLevelDestination.IDEA) {
            state.selectedDestination = TopLevelDestination.IDEA
        }
    }
}
