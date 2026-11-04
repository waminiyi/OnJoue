package com.sotopatrick.onjoue.data.sources.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

object Migration1To2 : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE tags RENAME COLUMN libelle TO label")
        connection.execSQL("ALTER TABLE tags RENAME COLUMN groupe TO groupName")
        connection.execSQL("ALTER TABLE tags ADD COLUMN normalizedLabel TEXT NOT NULL DEFAULT ''")
        connection.execSQL("ALTER TABLE materiels RENAME TO equipment")
        connection.execSQL("ALTER TABLE equipment RENAME COLUMN libelle TO label")
        connection.execSQL("ALTER TABLE equipment ADD COLUMN normalizedLabel TEXT NOT NULL DEFAULT ''")
        val groups = mapOf("LIEU" to "LOCATION", "SAISON" to "SEASON", "MOMENT" to "TIME", "AMBIANCE" to "MOOD")
        for ((old, new) in groups) {
            connection.prepare("UPDATE tags SET groupName = ? WHERE groupName = ?").use {
                it.bindText(1, new)
                it.bindText(2, old)
                it.step()
            }
        }
        for (table in listOf("tags", "equipment")) {
            val labels = buildList {
                connection.prepare("SELECT id, label FROM $table").use {
                    while (it.step()) add(it.getText(0) to it.getText(1))
                }
            }
            for ((id, label) in labels) {
                connection.prepare("UPDATE $table SET label = ?, normalizedLabel = ? WHERE id = ?").use {
                    it.bindText(1, label.trim())
                    it.bindText(2, label.trim().lowercase())
                    it.bindText(3, id)
                    it.step()
                }
            }
        }
        connection.execSQL("CREATE UNIQUE INDEX index_tags_groupName_normalizedLabel ON tags(groupName, normalizedLabel)")
        connection.execSQL("CREATE UNIQUE INDEX index_equipment_normalizedLabel ON equipment(normalizedLabel)")
        connection.execSQL("""
            CREATE TABLE activities (
                id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, description TEXT NOT NULL,
                note TEXT, minAge INTEGER NOT NULL, maxAge INTEGER NOT NULL,
                duration INTEGER NOT NULL, image TEXT, document TEXT
            )
        """.trimIndent())
        connection.execSQL("""
            CREATE TABLE steps (
                activityId TEXT NOT NULL, position INTEGER NOT NULL, text TEXT NOT NULL, timerSeconds INTEGER,
                PRIMARY KEY(activityId, position),
                FOREIGN KEY(activityId) REFERENCES activities(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())
        connection.execSQL("""
            CREATE TABLE variants (
                activityId TEXT NOT NULL, position INTEGER NOT NULL, text TEXT NOT NULL,
                PRIMARY KEY(activityId, position),
                FOREIGN KEY(activityId) REFERENCES activities(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())
        for ((table, column, parent) in listOf(
            Triple("activity_tags", "tagId", "tags"),
            Triple("activity_equipment", "equipmentId", "equipment"),
        )) {
            connection.execSQL("""
                CREATE TABLE $table (
                    activityId TEXT NOT NULL, $column TEXT NOT NULL,
                    PRIMARY KEY(activityId, $column),
                    FOREIGN KEY(activityId) REFERENCES activities(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY($column) REFERENCES $parent(id) ON UPDATE NO ACTION ON DELETE RESTRICT
                )
            """.trimIndent())
            connection.execSQL("CREATE INDEX index_${table}_$column ON $table($column)")
        }
        connection.execSQL("""
            CREATE TABLE reviews (
                id TEXT NOT NULL PRIMARY KEY, activityId TEXT NOT NULL, rating TEXT NOT NULL, comment TEXT NOT NULL,
                FOREIGN KEY(activityId) REFERENCES activities(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())
        connection.execSQL("CREATE INDEX index_reviews_activityId ON reviews(activityId)")
        connection.execSQL("""
            CREATE TABLE favorites (
                id TEXT NOT NULL PRIMARY KEY, activityId TEXT NOT NULL,
                FOREIGN KEY(activityId) REFERENCES activities(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())
        connection.execSQL("CREATE UNIQUE INDEX index_favorites_activityId ON favorites(activityId)")
    }
}
