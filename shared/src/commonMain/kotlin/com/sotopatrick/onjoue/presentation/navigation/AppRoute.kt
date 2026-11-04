package com.sotopatrick.onjoue.presentation.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable data object Idea : AppRoute
    @Serializable data object ActivitySheets : AppRoute
    @Serializable data object Favorites : AppRoute
    @Serializable data object Settings : AppRoute
    @Serializable data object TagManagement : AppRoute
    @Serializable data object EquipmentManagement : AppRoute
    @Serializable data object ActivityManagement : AppRoute
}

enum class TopLevelDestination(val route: AppRoute) {
    IDEA(AppRoute.Idea),
    ACTIVITY_SHEETS(AppRoute.ActivitySheets),
    FAVORITES(AppRoute.Favorites),
    SETTINGS(AppRoute.Settings),
}

internal val NavigationSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(AppRoute.Idea::class, AppRoute.Idea.serializer())
            subclass(AppRoute.ActivitySheets::class, AppRoute.ActivitySheets.serializer())
            subclass(AppRoute.Favorites::class, AppRoute.Favorites.serializer())
            subclass(AppRoute.Settings::class, AppRoute.Settings.serializer())
            subclass(AppRoute.TagManagement::class, AppRoute.TagManagement.serializer())
            subclass(AppRoute.EquipmentManagement::class, AppRoute.EquipmentManagement.serializer())
            subclass(AppRoute.ActivityManagement::class, AppRoute.ActivityManagement.serializer())
        }
    }
}
