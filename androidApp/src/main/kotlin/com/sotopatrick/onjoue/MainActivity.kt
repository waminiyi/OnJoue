package com.sotopatrick.onjoue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.sotopatrick.onjoue.data.repositories.activity.RoomActivityRepository
import com.sotopatrick.onjoue.data.repositories.equipment.RoomEquipmentRepository
import com.sotopatrick.onjoue.data.repositories.tag.RoomTagRepository
import com.sotopatrick.onjoue.data.sources.room.getAppDatabase
import com.sotopatrick.onjoue.data.sources.room.seedInitialCatalog
import com.sotopatrick.onjoue.presentation.errors.FrenchErrorParser
import com.sotopatrick.onjoue.presentation.activity.ActivitySheetsViewModel
import com.sotopatrick.onjoue.presentation.activity.ActivityDetailsViewModel
import com.sotopatrick.onjoue.presentation.management.ManagementViewModel
import com.sotopatrick.onjoue.presentation.startup.StartupViewModel
import com.sotopatrick.onjoue.presentation.settings.CatalogResetViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val database = getAppDatabase(applicationContext)
        val activities = RoomActivityRepository(database)
        val tags = RoomTagRepository(database, debug = BuildConfig.DEBUG)
        val createManagement: (() -> ManagementViewModel)? = if (BuildConfig.DEBUG) {
            val equipmentItems = RoomEquipmentRepository(database, debug = BuildConfig.DEBUG)
            val factory = { ManagementViewModel(tags, equipmentItems, FrenchErrorParser()) }
            factory
        } else null
        setContent {
            App(
                createManagement,
                createStartup = {
                    StartupViewModel(tags, FrenchErrorParser()) { seedInitialCatalog(database) }
                },
                createActivitySheets = { ActivitySheetsViewModel(activities, FrenchErrorParser()) },
                createActivityDetails = { id -> ActivityDetailsViewModel(activities, id, FrenchErrorParser()) },
                createCatalogReset = if (BuildConfig.DEBUG) {
                    { CatalogResetViewModel(FrenchErrorParser()) { seedInitialCatalog(database, forceReset = true) } }
                } else null,
            )
        }
    }
}

@Preview
@Composable
private fun AppAndroidPreview() {
    App()
}
