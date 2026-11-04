@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.sotopatrick.onjoue.data.repositories

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.sotopatrick.onjoue.data.models.common.AppException
import com.sotopatrick.onjoue.data.models.equipment.EquipmentId
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.data.models.tag.TagId
import com.sotopatrick.onjoue.data.repositories.activity.RoomActivityRepository
import com.sotopatrick.onjoue.data.repositories.equipment.RoomEquipmentRepository
import com.sotopatrick.onjoue.data.repositories.tag.RoomTagRepository
import com.sotopatrick.onjoue.data.sources.room.AppDatabase
import com.sotopatrick.onjoue.data.sources.room.DatabaseOperation
import com.sotopatrick.onjoue.data.sources.room.configure
import com.sotopatrick.onjoue.data.sources.room.equipment.EquipmentEntity
import com.sotopatrick.onjoue.data.sources.room.transaction
import kotlin.test.*
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory

class RoomRepositoriesIosTest {
    @Test
    fun bundledDriverCreatesAndRetainsInitialCatalog() = runTest {
        val path = NSTemporaryDirectory() + "onjoue-catalog-${Uuid.random()}.db"
        val initial = Room.databaseBuilder<AppDatabase>(name = path).configure()
        val deletedId = try {
            verifyInitialCatalog(initial)
            val tags = RoomTagRepository(initial, true)
            val game = tags.observeTags().first().single { it.label == "Jeu" }
            tags.deleteTag(game.id)
            game.id
        } finally { initial.close() }
        val reopened = Room.databaseBuilder<AppDatabase>(name = path).configure()
        try {
            assertNull(RoomTagRepository(reopened).getTag(deletedId))
            assertEquals(40, RoomTagRepository(reopened).observeTags().first().size)
        } finally {
            reopened.close()
            cleanUp(path)
        }
    }

    @Test
    fun bundledDriverObservesDistinctCatalogUsage() = runTest {
        val path = NSTemporaryDirectory() + "onjoue-usage-${Uuid.random()}.db"
        val database = Room.databaseBuilder<AppDatabase>(name = path).configure(seedCatalog = false)
        try {
            verifyCatalogUsage(database)
        } finally {
            database.close()
            cleanUp(path)
        }
    }

    @Test
    fun bundledDriverEnforcesRelationsAndAtomicAggregates() = runTest {
        val path = NSTemporaryDirectory() + "onjoue-test-${Uuid.random()}.db"
        val database = Room.databaseBuilder<AppDatabase>(name = path).configure(seedCatalog = false)
        try {
            verifyRepositoryRelations(database)
            assertFailsWith<AppException.NotFound> {
                database.transaction(DatabaseOperation.INSERT) {
                    database.equipmentDao().insert(EquipmentEntity("rollback", "Ballon", "ballon"))
                    throw AppException.NotFound()
                }
            }
            assertNull(database.equipmentDao().get("rollback"))
        } finally {
            database.close()
            cleanUp(path)
        }
    }

    @Test
    fun reopeningRetainsAccentsAndCompleteActivity() = runTest {
        val path = NSTemporaryDirectory() + "onjoue-test-${Uuid.random()}.db"
        fun open() = Room.databaseBuilder<AppDatabase>(name = path).configure(seedCatalog = false)
        val item = sampleEquipment("Ballon")
        val summer = sampleTag("Été", TagGroup.SEASON)
        val activity = sampleActivity().copy(tags = setOf(summer), equipment = setOf(item))
        val first = open()
        val tag = try {
            val tags = RoomTagRepository(first, true)
            tags.save(summer.copy(label = " Été "))
            assertFailsWith<AppException.Conflict> { sampleTag("été", TagGroup.SEASON).also { tags.save(it) } }
            RoomEquipmentRepository(first, true).save(item)
            RoomActivityRepository(first, true).save(activity)
            summer
        } finally { first.close() }
        val second = open()
        try {
            assertEquals(tag, RoomTagRepository(second).getTag(tag.id))
            assertEquals(activity, RoomActivityRepository(second).observeActivities().first().single())
            assertEquals(1, RoomTagRepository(second).observeTagUsage().first().single().activityCount)
            assertEquals(1, RoomEquipmentRepository(second).observeEquipmentUsage().first().single().activityCount)
        } finally {
            second.close()
            cleanUp(path)
        }
    }

    @Test
    fun bundledDriverMigratesVersionOneWithoutLosingCatalogData() = runTest {
        val path = NSTemporaryDirectory() + "onjoue-migration-${Uuid.random()}.db"
        try {
            BundledSQLiteDriver().open(path).use { connection ->
                connection.execSQL("CREATE TABLE tags(id TEXT NOT NULL PRIMARY KEY, libelle TEXT NOT NULL, groupe TEXT NOT NULL, parent TEXT, FOREIGN KEY(parent) REFERENCES tags(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
                connection.execSQL("CREATE INDEX index_tags_parent ON tags(parent)")
                connection.execSQL("CREATE TABLE materiels(id TEXT NOT NULL PRIMARY KEY, libelle TEXT NOT NULL)")
                connection.execSQL("INSERT INTO tags VALUES('inside', 'Dedans', 'LIEU', NULL), ('living', 'Salon', 'LIEU', 'inside'), ('summer', 'Été', 'SAISON', NULL)")
                connection.execSQL("INSERT INTO materiels VALUES('clay', 'Pâte à modeler')")
                connection.execSQL("PRAGMA user_version = 1")
            }
            val database = Room.databaseBuilder<AppDatabase>(name = path).configure()
            try {
                val tags = RoomTagRepository(database, true)
                assertEquals(TagId("inside"), tags.getTag(TagId("living"))?.parent)
                assertEquals(TagGroup.SEASON, tags.getTag(TagId("summer"))?.group)
                assertEquals("Pâte à modeler", RoomEquipmentRepository(database).getEquipment(EquipmentId("clay"))?.label)
                assertFailsWith<AppException.Conflict> { sampleTag("été", TagGroup.SEASON).also { tags.save(it) } }
                assertFailsWith<AppException.InUse> { tags.deleteTag(TagId("inside")) }
                RoomActivityRepository(database, true).save(sampleActivity())
            } finally { database.close() }
        } finally { cleanUp(path) }
    }

    private fun cleanUp(path: String) {
        for (file in listOf(path, "$path-wal", "$path-shm")) {
            if (NSFileManager.defaultManager.fileExistsAtPath(file)) {
                assertTrue(NSFileManager.defaultManager.removeItemAtPath(file, null), "Could not remove $file")
            }
        }
    }
}
