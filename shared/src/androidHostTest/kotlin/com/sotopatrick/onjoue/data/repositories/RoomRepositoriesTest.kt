package com.sotopatrick.onjoue.data.repositories

import android.content.Context
import androidx.room.Room
import androidx.room.useWriterConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import com.sotopatrick.onjoue.data.models.activity.*
import com.sotopatrick.onjoue.data.models.common.AppException
import com.sotopatrick.onjoue.data.models.equipment.EquipmentId
import com.sotopatrick.onjoue.data.models.tag.*
import com.sotopatrick.onjoue.data.repositories.activity.RoomActivityRepository
import com.sotopatrick.onjoue.data.repositories.equipment.RoomEquipmentRepository
import com.sotopatrick.onjoue.data.repositories.tag.RoomTagRepository
import com.sotopatrick.onjoue.data.sources.room.AppDatabase
import com.sotopatrick.onjoue.data.sources.room.DatabaseOperation
import com.sotopatrick.onjoue.data.sources.room.InitialCatalogCallback
import com.sotopatrick.onjoue.presentation.errors.FrenchErrorParser
import com.sotopatrick.onjoue.presentation.startup.StartupViewModel
import com.sotopatrick.onjoue.data.sources.room.databaseAccess
import com.sotopatrick.onjoue.data.sources.room.equipment.EquipmentEntity
import com.sotopatrick.onjoue.data.sources.room.migrations.Migration1To2
import com.sotopatrick.onjoue.data.sources.room.migrations.Migration2To3
import com.sotopatrick.onjoue.data.sources.room.tag.TagEntity
import com.sotopatrick.onjoue.data.sources.room.transaction
import java.io.File
import kotlin.test.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoomRepositoriesTest {
    private lateinit var database: AppDatabase
    private lateinit var tags: RoomTagRepository
    private lateinit var equipment: RoomEquipmentRepository
    private val context: Context get() = RuntimeEnvironment.getApplication()

    private fun open(path: String? = null, seedCatalog: Boolean = false): AppDatabase {
        val builder = if (path == null) Room.inMemoryDatabaseBuilder<AppDatabase>(context)
        else Room.databaseBuilder<AppDatabase>(context, path)
        if (seedCatalog) builder.addCallback(InitialCatalogCallback)
        return builder.setDriver(AndroidSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO)
            .addMigrations(Migration1To2, Migration2To3).build()
    }

    @Before
    fun setUp() {
        database = open()
        tags = RoomTagRepository(database, debug = true)
        equipment = RoomEquipmentRepository(database, debug = true)
    }

    @After
    fun close() { database.close() }

    @Test
    fun catalogUsageIncludesChildrenWithoutDuplicatesAndObservesAssociationChanges() = runTest {
        verifyCatalogUsage(database)
    }

    @Test
    fun initialCatalogIsAvailableInReleaseAndNeverResetsEditsOrDeletions() = runTest {
        val path = File(context.cacheDir, "initial-catalog.db").absolutePath
        try {
            val initial = open(path, seedCatalog = true)
            val removedId = try {
                val startup = StartupViewModel(RoomTagRepository(initial), FrenchErrorParser())
                assertTrue(startup.state.first { it.ready || it.error != null }.ready)
                verifyInitialCatalog(initial)
                val repository = RoomTagRepository(initial, true)
                val game = repository.observeTags().first().single { it.label == "Jeu" }
                repository.deleteTag(game.id)
                val ball = RoomEquipmentRepository(initial, true)
                val item = ball.observeEquipment().first().single { it.label == "Ballon" }
                ball.save(item.copy(label = "Grand ballon"))
                game.id
            } finally { initial.close() }
            val reopened = open(path, seedCatalog = true)
            try {
                assertNull(RoomTagRepository(reopened).getTag(removedId))
                assertEquals(40, RoomTagRepository(reopened).observeTags().first().size)
                assertTrue(RoomEquipmentRepository(reopened).observeEquipment().first().any { it.label == "Grand ballon" })
            } finally { reopened.close() }
        } finally { context.deleteDatabase(path) }
    }

    @Test
    fun versionTwoCatalogMigrationReusesExistingIdsAndPreservesActivities() = runTest {
        val path = File(context.cacheDir, "initial-catalog-migration.db").absolutePath
        val inside = sampleTag("Dedans", TagGroup.LOCATION)
        val summer = sampleTag("Été", TagGroup.SEASON)
        val ball = sampleEquipment("Ballon")
        val activity = sampleActivity().copy(tags = setOf(inside, summer), equipment = setOf(ball))
        try {
            val legacy = open(path)
            try {
                RoomTagRepository(legacy, true).save(inside)
                RoomTagRepository(legacy, true).save(summer)
                RoomEquipmentRepository(legacy, true).save(ball)
                RoomActivityRepository(legacy, true).save(activity)
            } finally { legacy.close() }
            AndroidSQLiteDriver().open(path).use { it.execSQL("PRAGMA user_version = 2") }
            val migrated = open(path, seedCatalog = true)
            try {
                val tags = RoomTagRepository(migrated).observeTags().first()
                assertEquals(41, tags.size)
                assertEquals(summer.id, tags.single { it.label == "Été" }.id)
                assertEquals(inside.id, tags.single { it.label == "Salon" }.parent)
                assertEquals(ball.id, RoomEquipmentRepository(migrated).observeEquipment().first().single { it.label == "Ballon" }.id)
                assertEquals(activity, RoomActivityRepository(migrated).getActivity(activity.id))
            } finally { migrated.close() }
        } finally { context.deleteDatabase(path) }
    }

    @Test
    fun identifierCollisionRollsBackCatalogMigrationAndIsShownAtStartup() = runTest {
        val path = File(context.cacheDir, "initial-catalog-conflict.db").absolutePath
        try {
            val legacy = open(path)
            try {
                RoomEquipmentRepository(legacy, true).save(
                    sampleEquipment("Matériel personnalisé", id = EquipmentId("catalog-equipment-ball")),
                )
            } finally { legacy.close() }
            AndroidSQLiteDriver().open(path).use { it.execSQL("PRAGMA user_version = 2") }
            val failing = open(path, seedCatalog = true)
            try {
                val startup = StartupViewModel(RoomTagRepository(failing), FrenchErrorParser())
                val state = startup.state.first { !it.loading }
                assertFalse(state.ready)
                assertEquals(FrenchErrorParser().parse(AppException.Conflict()), state.error)
                startup.load()
                assertNotNull(startup.state.first { !it.loading }.error)
            } finally { failing.close() }
            AndroidSQLiteDriver().open(path).use { connection ->
                connection.prepare("PRAGMA user_version").use {
                    assertTrue(it.step())
                    assertEquals(2L, it.getLong(0))
                }
                connection.prepare("SELECT COUNT(*) FROM tags").use {
                    assertTrue(it.step())
                    assertEquals(0L, it.getLong(0))
                }
                connection.prepare("SELECT label FROM equipment").use {
                    assertTrue(it.step())
                    assertEquals("Matériel personnalisé", it.getText(0))
                    assertFalse(it.step())
                }
            }
        } finally { context.deleteDatabase(path) }
    }

    @Test
    fun catalogCrudPreservesIdsAndAccents() = runTest {
        assertTrue(tags.observeTags().first().isEmpty())
        val tag = sampleTag("  Été  ", TagGroup.SEASON).also { tags.save(it) }
        val item = sampleEquipment(" Pâte à modeler ").also { equipment.save(it) }
        assertEquals(tag.copy(label = "Été"), tags.getTag(tag.id))
        assertEquals(item.copy(label = "Pâte à modeler"), equipment.getEquipment(item.id))
        sampleTag("Hiver", TagGroup.SEASON, id = tag.id).also { tags.save(it) }
        sampleEquipment("Crayons", id = item.id).also { equipment.save(it) }
        assertEquals(tag.copy(label = "Hiver"), tags.getTag(tag.id))
        assertEquals(item.copy(label = "Crayons"), equipment.getEquipment(item.id))
        tags.deleteTag(tag.id)
        equipment.deleteEquipment(item.id)
        assertNull(tags.getTag(tag.id))
        assertNull(equipment.getEquipment(item.id))
    }

    @Test
    fun labelsAreValidatedAndStorageEnforcesUniqueness() = runTest {
        for (blank in listOf("", " ", "\t\n")) {
            assertFailsWith<AppException.Validation> { sampleTag(blank, TagGroup.TYPE).also { tags.save(it) } }
            assertFailsWith<AppException.Validation> { sampleEquipment(blank).also { equipment.save(it) } }
        }
        sampleTag("Été", TagGroup.SEASON).also { tags.save(it) }
        sampleEquipment("Ballon").also { equipment.save(it) }
        assertFailsWith<AppException.Conflict> { sampleTag(" été ", TagGroup.SEASON).also { tags.save(it) } }
        assertFailsWith<AppException.Conflict> { sampleEquipment(" BALLON ").also { equipment.save(it) } }
        sampleTag("Été", TagGroup.OCCASION).also { tags.save(it) }
        val winter = sampleTag("Hiver", TagGroup.SEASON).also { tags.save(it) }
        val pencils = sampleEquipment("Crayons").also { equipment.save(it) }
        assertFailsWith<AppException.Conflict> { sampleTag("Été", TagGroup.SEASON, id = winter.id).also { tags.save(it) } }
        assertFailsWith<AppException.Conflict> { sampleEquipment("Ballon", id = pencils.id).also { equipment.save(it) } }
        assertEquals("Hiver", tags.getTag(winter.id)?.label)
        assertEquals("Crayons", equipment.getEquipment(pencils.id)?.label)
    }

    @Test
    fun hierarchyRemainsTwoLevelsAndParentsAreProtected() = runTest {
        val inside = sampleTag("Dedans", TagGroup.LOCATION).also { tags.save(it) }
        val livingRoom = sampleTag("Salon", TagGroup.LOCATION, inside.id).also { tags.save(it) }
        val calm = sampleTag("Calme", TagGroup.MOOD).also { tags.save(it) }
        assertFailsWith<AppException.InUse> { tags.deleteTag(inside.id) }
        assertFailsWith<AppException.Validation> { sampleTag("Dedans", TagGroup.TYPE, id = inside.id).also { tags.save(it) } }
        assertFailsWith<AppException.Validation> { sampleTag("Dedans", TagGroup.LOCATION, livingRoom.id, id = inside.id).also { tags.save(it) } }
        assertFailsWith<AppException.Validation> { sampleTag("Canapé", TagGroup.LOCATION, livingRoom.id).also { tags.save(it) } }
        assertFailsWith<AppException.Validation> { sampleTag("Salon", TagGroup.LOCATION, livingRoom.id, id = livingRoom.id).also { tags.save(it) } }
        assertFailsWith<AppException.Validation> { sampleTag("Actif", TagGroup.MOOD, inside.id).also { tags.save(it) } }
        assertFailsWith<AppException.Validation> { sampleTag("Cuisine", TagGroup.LOCATION, calm.id).also { tags.save(it) } }
        assertFailsWith<AppException.InvalidReference> { sampleTag("Cuisine", TagGroup.LOCATION, TagId("missing")).also { tags.save(it) } }
        sampleTag("Salon", TagGroup.LOCATION, null, id = livingRoom.id).also { tags.save(it) }
        tags.deleteTag(inside.id)
    }

    @Test
    fun allRepositoriesPersistAggregatesAndEnforceRelationships() = runTest {
        verifyRepositoryRelations(database)
    }

    @Test
    fun releaseCatalogMutationsAreForbidden() = runTest {
        val tag = sampleTag("Calme", TagGroup.MOOD).also { tags.save(it) }
        val item = sampleEquipment("Ballon").also { equipment.save(it) }
        val releaseTags = RoomTagRepository(database)
        val releaseEquipment = RoomEquipmentRepository(database)
        val releaseActivities = RoomActivityRepository(database)
        assertFailsWith<AppException.Forbidden> { sampleTag("Actif", TagGroup.MOOD).also { releaseTags.save(it) } }
        assertFailsWith<AppException.Forbidden> { sampleTag("Actif", TagGroup.MOOD, id = tag.id).also { releaseTags.save(it) } }
        assertFailsWith<AppException.Forbidden> { releaseTags.deleteTag(tag.id) }
        assertFailsWith<AppException.Forbidden> { sampleEquipment("Crayons").also { releaseEquipment.save(it) } }
        assertFailsWith<AppException.Forbidden> { sampleEquipment("Crayons", id = item.id).also { releaseEquipment.save(it) } }
        assertFailsWith<AppException.Forbidden> { releaseEquipment.deleteEquipment(item.id) }
        val activity = sampleActivity()
        assertFailsWith<AppException.Forbidden> { releaseActivities.save(activity) }
        assertFailsWith<AppException.Forbidden> { releaseActivities.save(activity) }
        assertFailsWith<AppException.Forbidden> { releaseActivities.deleteActivity(activity.id) }
        assertNotNull(releaseTags.getTag(tag.id))
    }

    @Test
    fun saveCreatesUnknownIdsAndDeletesRemainNotFound() = runTest {
        assertFailsWith<AppException.NotFound> { tags.deleteTag(TagId("missing")) }
        val savedTag = sampleTag("Lieu", TagGroup.LOCATION, id = TagId("missing")).also { tags.save(it) }
        assertEquals(TagId("missing"), savedTag.id)
        assertEquals(savedTag, tags.getTag(savedTag.id))
        sampleTag("Autre lieu", TagGroup.LOCATION, id = savedTag.id).also { tags.save(it) }
        assertEquals("Autre lieu", tags.getTag(savedTag.id)?.label)
        tags.deleteTag(savedTag.id)
        assertFailsWith<AppException.NotFound> { tags.deleteTag(TagId("missing")) }
        val savedEquipment = sampleEquipment("Objet", id = EquipmentId("missing")).also { equipment.save(it) }
        assertEquals(EquipmentId("missing"), savedEquipment.id)
        assertEquals(savedEquipment, equipment.getEquipment(savedEquipment.id))
        sampleEquipment("Autre objet", id = savedEquipment.id).also { equipment.save(it) }
        assertEquals("Autre objet", equipment.getEquipment(savedEquipment.id)?.label)
        equipment.deleteEquipment(savedEquipment.id)
        assertFailsWith<AppException.NotFound> { equipment.deleteEquipment(EquipmentId("missing")) }
        val activities = RoomActivityRepository(database, true)
        activities.save(sampleActivity())
        assertEquals(sampleActivity(), activities.getActivity(sampleActivity().id))
        assertFailsWith<AppException.NotFound> { activities.deleteActivity(ActivityId("missing")) }
    }

    @Test
    fun transactionFailuresRollbackAndCorruptionIsTyped() = runTest {
        assertFailsWith<AppException.NotFound> {
            database.transaction(DatabaseOperation.INSERT) {
                database.equipmentDao().insert(EquipmentEntity("test", "Ballon", "ballon"))
                throw AppException.NotFound()
            }
        }
        assertNull(database.equipmentDao().get("test"))
        database.tagDao().insert(TagEntity("invalid", "Tag", "UNKNOWN", null, "tag"))
        assertFailsWith<AppException.CorruptData> { tags.observeTags().first() }
        assertFailsWith<AppException.CorruptData> { tags.getTag(TagId("invalid")) }
    }

    @Test
    fun duplicateCatalogIdsAreRejectedByStorageWithoutReplacement() = runTest {
        val tag = TagEntity("same", "Original", "TYPE", null, "original")
        val item = EquipmentEntity("same", "Original", "original")
        database.tagDao().insert(tag)
        database.equipmentDao().insert(item)
        val tagConflict = assertFailsWith<AppException.Conflict> {
            databaseAccess(DatabaseOperation.INSERT) {
                database.tagDao().insert(tag.copy(label = "Changed", normalizedLabel = "changed"))
            }
        }
        assertNotNull(tagConflict.cause)
        assertFailsWith<AppException.Conflict> {
            databaseAccess(DatabaseOperation.INSERT) {
                database.equipmentDao().insert(item.copy(label = "Changed", normalizedLabel = "changed"))
            }
        }
        assertEquals(tag, database.tagDao().get("same"))
        assertEquals(item, database.equipmentDao().get("same"))
    }

    @Test
    fun closingDatabasePreservesRoomCancellation() = runTest {
        sampleEquipment("Ballon").also { equipment.save(it) }
        database.close()
        assertFailsWith<CancellationException> { sampleEquipment("Crayons").also { equipment.save(it) } }
    }

    @Test
    fun databaseReadFailuresAreTypedAndRetainCause() = runTest {
        sampleEquipment("Ballon").also { equipment.save(it) }
        database.useWriterConnection { connection ->
            connection.usePrepared("DROP TABLE equipment") { it.step() }
        }
        val error = assertFailsWith<AppException.Storage> { equipment.getEquipment(EquipmentId("missing")) }
        assertNotNull(error.cause)
        assertFailsWith<AppException.Storage> { equipment.observeEquipment().first() }
    }

    @Test
    fun concurrentWritesDoNotLoseDataAndOnlyOneDuplicateSucceeds() = runTest {
        (1..10).flatMap { number ->
            listOf(
                async { sampleTag("Tag $number", TagGroup.TYPE).also { tags.save(it) } },
                async { sampleEquipment("Matériel $number").also { equipment.save(it) } },
            )
        }.awaitAll()
        assertEquals(10, tags.observeTags().first().size)
        assertEquals(10, equipment.observeEquipment().first().size)
        val results = (1..5).map {
            async {
                try {
                    sampleEquipment("Doublon").also { equipment.save(it) }
                    true
                } catch (_: AppException.Conflict) { false }
            }
        }.awaitAll()
        assertEquals(1, results.count { it })
    }

    @Test
    fun concurrentSavesWithSameIdKeepOneRow() = runTest {
        val id = EquipmentId("shared")
        (1..5).map { number ->
            async { sampleEquipment("Objet $number", id).also { equipment.save(it) } }
        }.awaitAll()
        val stored = equipment.observeEquipment().first().single()
        assertEquals(id, stored.id)
        assertTrue(stored.label in (1..5).map { "Objet $it" })
    }

    @Test
    fun reopeningPreservesCompleteAggregate() = runTest {
        val path = File(context.cacheDir, "persistence-test.db").absolutePath
        val parent = sampleTag("Dedans", TagGroup.LOCATION)
        val tag = sampleTag("Salon", TagGroup.LOCATION, parent.id)
        val item = sampleEquipment("Ballon")
        val activity = sampleActivity().copy(tags = setOf(tag), equipment = setOf(item))
        try {
            open(path).let { first ->
                try {
                    RoomTagRepository(first, true).apply { save(parent); save(tag) }
                    RoomEquipmentRepository(first, true).save(item)
                    RoomActivityRepository(first, true).save(activity)
                }
                finally { first.close() }
            }
            open(path).let { second ->
                try { assertEquals(activity, RoomActivityRepository(second).getActivity(activity.id)) }
                finally { second.close() }
            }
        } finally { context.deleteDatabase(path) }
    }

    @Test
    fun migrationPreservesLegacyIdsLabelsGroupsAndParents() = runTest {
        val path = File(context.cacheDir, "migration-test.db").absolutePath
        try {
            AndroidSQLiteDriver().open(path).use { connection ->
                connection.execSQL("CREATE TABLE tags(id TEXT NOT NULL PRIMARY KEY, libelle TEXT NOT NULL, groupe TEXT NOT NULL, parent TEXT, FOREIGN KEY(parent) REFERENCES tags(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
                connection.execSQL("CREATE INDEX index_tags_parent ON tags(parent)")
                connection.execSQL("CREATE TABLE materiels(id TEXT NOT NULL PRIMARY KEY, libelle TEXT NOT NULL)")
                connection.execSQL("INSERT INTO tags VALUES('inside', 'Dedans', 'LIEU', NULL), ('living', 'Salon', 'LIEU', 'inside'), ('summer', 'Été', 'SAISON', NULL)")
                connection.execSQL("INSERT INTO materiels VALUES('clay', 'Pâte à modeler')")
                connection.execSQL("PRAGMA user_version = 1")
            }
            open(path).let { migrated ->
                try {
                    val repository = RoomTagRepository(migrated, true)
                    assertEquals(TagId("inside"), repository.getTag(TagId("living"))?.parent)
                    assertEquals(TagGroup.SEASON, repository.getTag(TagId("summer"))?.group)
                    assertEquals("Pâte à modeler", RoomEquipmentRepository(migrated).getEquipment(EquipmentId("clay"))?.label)
                    assertFailsWith<AppException.Conflict> { sampleTag("été", TagGroup.SEASON).also { repository.save(it) } }
                    assertFailsWith<AppException.InUse> { repository.deleteTag(TagId("inside")) }
                    RoomActivityRepository(migrated, true).save(sampleActivity())
                } finally { migrated.close() }
            }
        } finally { context.deleteDatabase(path) }
    }

    @Test
    fun failedMigrationLeavesLegacyDataIntact() = runTest {
        val path = File(context.cacheDir, "migration-conflict-test.db").absolutePath
        try {
            AndroidSQLiteDriver().open(path).use { connection ->
                connection.execSQL("CREATE TABLE tags(id TEXT NOT NULL PRIMARY KEY, libelle TEXT NOT NULL, groupe TEXT NOT NULL, parent TEXT, FOREIGN KEY(parent) REFERENCES tags(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
                connection.execSQL("CREATE INDEX index_tags_parent ON tags(parent)")
                connection.execSQL("CREATE TABLE materiels(id TEXT NOT NULL PRIMARY KEY, libelle TEXT NOT NULL)")
                connection.execSQL("INSERT INTO tags VALUES('first', 'Été', 'SAISON', NULL), ('second', 'été', 'SAISON', NULL)")
                connection.execSQL("PRAGMA user_version = 1")
            }
            val failing = open(path)
            try {
                assertFailsWith<AppException.Conflict> { RoomTagRepository(failing).getTag(TagId("first")) }
            } finally { failing.close() }
            AndroidSQLiteDriver().open(path).use { connection ->
                connection.prepare("PRAGMA user_version").use {
                    assertTrue(it.step())
                    assertEquals(1L, it.getLong(0))
                }
                connection.prepare("SELECT libelle FROM tags ORDER BY id").use {
                    assertTrue(it.step())
                    assertEquals("Été", it.getText(0))
                    assertTrue(it.step())
                    assertEquals("été", it.getText(0))
                }
            }
        } finally { context.deleteDatabase(path) }
    }
}
