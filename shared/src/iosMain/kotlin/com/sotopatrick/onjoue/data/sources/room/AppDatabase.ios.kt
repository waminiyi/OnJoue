@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.sotopatrick.onjoue.data.sources.room

import androidx.room.Room
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

fun createAppDatabase(): AppDatabase {
    val directory = NSFileManager.defaultManager.URLForDirectory(
        NSApplicationSupportDirectory, NSUserDomainMask, null, true, null,
    )?.path ?: error("Impossible d'ouvrir le répertoire de la base locale.")
    return Room.databaseBuilder<AppDatabase>(name = "$directory/onjoue.db").configure()
}
