package com.sotopatrick.onjoue.data.sources.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

object Migration6To7 : Migration(6, 7) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS tried_activities (id TEXT NOT NULL, activityId TEXT NOT NULL, PRIMARY KEY(id), FOREIGN KEY(activityId) REFERENCES activities(id) ON DELETE CASCADE)",
        )
        connection.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_tried_activities_activityId ON tried_activities(activityId)",
        )
    }
}
