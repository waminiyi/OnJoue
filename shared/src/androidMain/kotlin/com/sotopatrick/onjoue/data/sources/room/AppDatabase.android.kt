package com.sotopatrick.onjoue.data.sources.room

import android.content.Context
import androidx.room.Room

@Volatile
private var instance: AppDatabase? = null

fun getAppDatabase(context: Context): AppDatabase = instance ?: synchronized(AppDatabase::class.java) {
    instance ?: createAppDatabase(context).also { instance = it }
}

fun createAppDatabase(context: Context): AppDatabase {
    val application = context.applicationContext
    return Room.databaseBuilder<AppDatabase>(
        application, application.getDatabasePath("onjoue.db").absolutePath,
    ).configure()
}
