package com.sotopatrick.onjoue.data.sources.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
object Migration2To3 : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) = Unit
}
