package com.sotopatrick.onjoue.data.sources.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.sotopatrick.onjoue.data.sources.room.activity.ActivityDao
import com.sotopatrick.onjoue.data.sources.room.activity.ActivityEntity
import com.sotopatrick.onjoue.data.sources.room.activity.ActivityEquipmentDao
import com.sotopatrick.onjoue.data.sources.room.activity.ActivityEquipmentEntity
import com.sotopatrick.onjoue.data.sources.room.activity.ActivityTagDao
import com.sotopatrick.onjoue.data.sources.room.activity.ActivityTagEntity
import com.sotopatrick.onjoue.data.sources.room.activity.StepDao
import com.sotopatrick.onjoue.data.sources.room.activity.StepEntity
import com.sotopatrick.onjoue.data.sources.room.activity.VariantDao
import com.sotopatrick.onjoue.data.sources.room.activity.VariantEntity
import com.sotopatrick.onjoue.data.sources.room.equipment.EquipmentDao
import com.sotopatrick.onjoue.data.sources.room.equipment.EquipmentEntity
import com.sotopatrick.onjoue.data.sources.room.favorite.FavoriteDao
import com.sotopatrick.onjoue.data.sources.room.favorite.FavoriteEntity
import com.sotopatrick.onjoue.data.sources.room.migrations.Migration1To2
import com.sotopatrick.onjoue.data.sources.room.migrations.Migration2To3
import com.sotopatrick.onjoue.data.sources.room.migrations.Migration3To4
import com.sotopatrick.onjoue.data.sources.room.migrations.Migration4To5
import com.sotopatrick.onjoue.data.sources.room.migrations.Migration5To6
import com.sotopatrick.onjoue.data.sources.room.migrations.Migration6To7
import com.sotopatrick.onjoue.data.sources.room.migrations.Migration7To8
import com.sotopatrick.onjoue.data.sources.room.migrations.Migration8To9
import com.sotopatrick.onjoue.data.sources.room.review.ReviewDao
import com.sotopatrick.onjoue.data.sources.room.review.ReviewEntity
import com.sotopatrick.onjoue.data.sources.room.tag.TagDao
import com.sotopatrick.onjoue.data.sources.room.tag.TagEntity
import com.sotopatrick.onjoue.data.sources.room.tried.TriedDao
import com.sotopatrick.onjoue.data.sources.room.tried.TriedEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [
        TagEntity::class, EquipmentEntity::class, ActivityEntity::class, StepEntity::class,
        VariantEntity::class, ActivityTagEntity::class, ActivityEquipmentEntity::class,
        ReviewEntity::class, FavoriteEntity::class, TriedEntity::class,
        CatalogStateEntity::class,
    ], version = 9, exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catalogStateDao(): CatalogStateDao
    abstract fun tagDao(): TagDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun activityDao(): ActivityDao
    abstract fun stepDao(): StepDao
    abstract fun variantDao(): VariantDao
    abstract fun activityTagDao(): ActivityTagDao
    abstract fun activityEquipmentDao(): ActivityEquipmentDao
    abstract fun reviewDao(): ReviewDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun triedDao(): TriedDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun RoomDatabase.Builder<AppDatabase>.configure(): AppDatabase {
    setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(
            Migration1To2, Migration2To3, Migration3To4, Migration4To5, Migration5To6, Migration6To7, Migration7To8,
            Migration8To9,
        )
    return build()
}
