package com.sotopatrick.onjoue.data.sources.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

object Migration3To4 : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "DELETE FROM activities WHERE id IN (" +
                "'seed-activity-circuit-etoiles', 'seed-activity-galerie-formes', 'seed-activity-tournee-peluches')",
        )
    }
}
