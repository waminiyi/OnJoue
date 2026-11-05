package com.sotopatrick.onjoue.data.sources.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

object Migration4To5 : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE catalog_state (id INTEGER NOT NULL PRIMARY KEY, catalogVersion INTEGER NOT NULL)",
        )
    }
}
