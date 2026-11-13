package com.sotopatrick.onjoue

import androidx.compose.ui.window.ComposeUIViewController
import com.sotopatrick.onjoue.data.repositories.activity.RoomActivityRepository
import com.sotopatrick.onjoue.data.repositories.tag.RoomTagRepository
import com.sotopatrick.onjoue.data.sources.room.createAppDatabase
import com.sotopatrick.onjoue.data.sources.room.seedInitialCatalog
import com.sotopatrick.onjoue.presentation.errors.FrenchErrorParser
import com.sotopatrick.onjoue.presentation.activity.ActivitySheetsViewModel
import com.sotopatrick.onjoue.presentation.activity.ActivityDetailsViewModel
import com.sotopatrick.onjoue.presentation.startup.StartupViewModel

private val appDatabase by lazy { createAppDatabase() }

// Named like the UIViewController it returns, as called from Swift.
@Suppress("FunctionNaming")
fun MainViewController(): platform.UIKit.UIViewController {
    val tags = RoomTagRepository(appDatabase)
    val activities = RoomActivityRepository(appDatabase)
    return ComposeUIViewController {
        App(
            createStartup = { StartupViewModel(tags, FrenchErrorParser()) { seedInitialCatalog(appDatabase) } },
            createActivitySheets = { ActivitySheetsViewModel(activities, FrenchErrorParser()) },
            createActivityDetails = { id -> ActivityDetailsViewModel(activities, id, FrenchErrorParser()) },
        )
    }
}
