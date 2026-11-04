package com.sotopatrick.onjoue

import androidx.compose.ui.window.ComposeUIViewController
import com.sotopatrick.onjoue.data.repositories.equipment.RoomEquipmentRepository
import com.sotopatrick.onjoue.data.repositories.tag.RoomTagRepository
import com.sotopatrick.onjoue.data.sources.room.createAppDatabase
import com.sotopatrick.onjoue.presentation.errors.FrenchErrorParser
import com.sotopatrick.onjoue.presentation.management.ManagementViewModel
import com.sotopatrick.onjoue.presentation.startup.StartupViewModel
import kotlin.native.Platform

private val appDatabase by lazy { createAppDatabase() }

@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
fun MainViewController(debug: Boolean = false): platform.UIKit.UIViewController {
    val tags = RoomTagRepository(appDatabase, debug = debug && Platform.isDebugBinary)
    val createManagement: (() -> ManagementViewModel)? = if (debug && Platform.isDebugBinary) {
        val equipmentItems = RoomEquipmentRepository(appDatabase, debug = true)
        val factory = { ManagementViewModel(tags, equipmentItems, FrenchErrorParser()) }
        factory
    } else null
    return ComposeUIViewController {
        App(createManagement, createStartup = { StartupViewModel(tags, FrenchErrorParser()) })
    }
}