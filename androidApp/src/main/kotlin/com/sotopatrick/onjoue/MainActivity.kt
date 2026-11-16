package com.sotopatrick.onjoue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.sotopatrick.onjoue.data.repositories.activity.RoomActivityRepository
import com.sotopatrick.onjoue.data.repositories.favorite.RoomFavoriteRepository
import com.sotopatrick.onjoue.data.repositories.tag.RoomTagRepository
import com.sotopatrick.onjoue.data.sources.room.getAppDatabase
import com.sotopatrick.onjoue.data.sources.room.seedInitialCatalog
import com.sotopatrick.onjoue.presentation.errors.FrenchErrorParser
import com.sotopatrick.onjoue.presentation.activity.ActivitySheetsViewModel
import com.sotopatrick.onjoue.presentation.activity.ActivityDetailsViewModel
import com.sotopatrick.onjoue.presentation.startup.StartupViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val database = getAppDatabase(applicationContext)
        val activities = RoomActivityRepository(database)
        val tags = RoomTagRepository(database)
        setContent {
            App(
                createStartup = {
                    StartupViewModel(tags, FrenchErrorParser()) { seedInitialCatalog(database) }
                },
                createActivitySheets = {
                    ActivitySheetsViewModel(activities, FrenchErrorParser(), RoomFavoriteRepository(database), tags)
                },
                createActivityDetails = { id -> ActivityDetailsViewModel(activities, id, FrenchErrorParser()) },
            )
        }
    }
}

@Preview
@Composable
private fun AppAndroidPreview() {
    App()
}
