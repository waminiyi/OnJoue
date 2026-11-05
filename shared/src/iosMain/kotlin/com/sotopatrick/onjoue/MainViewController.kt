package com.sotopatrick.onjoue

import androidx.compose.ui.window.ComposeUIViewController
import com.sotopatrick.onjoue.data.repositories.activity.RoomActivityRepository
import com.sotopatrick.onjoue.data.repositories.equipment.RoomEquipmentRepository
import com.sotopatrick.onjoue.data.repositories.tag.RoomTagRepository
import com.sotopatrick.onjoue.data.sources.room.createAppDatabase
import com.sotopatrick.onjoue.data.sources.room.seedInitialCatalog
import com.sotopatrick.onjoue.presentation.errors.FrenchErrorParser
import com.sotopatrick.onjoue.presentation.activity.ActivitySheetsViewModel
import com.sotopatrick.onjoue.presentation.activity.ActivityDetailsViewModel
import com.sotopatrick.onjoue.presentation.management.ManagementViewModel
import com.sotopatrick.onjoue.presentation.startup.StartupViewModel
import com.sotopatrick.onjoue.presentation.settings.CatalogResetViewModel
import kotlin.native.Platform

private val appDatabase by lazy { createAppDatabase() }

// Named like the UIViewController it returns, as called from Swift.
@Suppress("FunctionNaming")
@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
fun MainViewController(debug: Boolean = false): platform.UIKit.UIViewController {
    val tags = RoomTagRepository(appDatabase, debug = debug && Platform.isDebugBinary)
    val activities = RoomActivityRepository(appDatabase)
    val createManagement: (() -> ManagementViewModel)? = if (debug && Platform.isDebugBinary) {
        val equipmentItems = RoomEquipmentRepository(appDatabase, debug = true)
        val factory = { ManagementViewModel(tags, equipmentItems, FrenchErrorParser()) }
        factory
    } else null
    return ComposeUIViewController {
        App(
            createManagement,
            createStartup = { StartupViewModel(tags, FrenchErrorParser()) { seedInitialCatalog(appDatabase) } },
            createActivitySheets = { ActivitySheetsViewModel(activities, FrenchErrorParser()) },
            createActivityDetails = { id -> ActivityDetailsViewModel(activities, id, FrenchErrorParser()) },
            createCatalogReset = if (debug && Platform.isDebugBinary) {
                { CatalogResetViewModel(FrenchErrorParser()) { seedInitialCatalog(appDatabase, forceReset = true) } }
            } else null,
        )
    }
}
