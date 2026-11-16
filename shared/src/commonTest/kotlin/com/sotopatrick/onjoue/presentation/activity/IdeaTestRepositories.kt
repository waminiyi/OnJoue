package com.sotopatrick.onjoue.presentation.activity

import com.sotopatrick.onjoue.data.models.activity.*
import com.sotopatrick.onjoue.data.models.equipment.EquipmentId
import com.sotopatrick.onjoue.data.models.favorite.*
import com.sotopatrick.onjoue.data.models.tag.*
import com.sotopatrick.onjoue.data.repositories.activity.ActivityRepository
import com.sotopatrick.onjoue.data.repositories.favorite.FavoriteRepository
import com.sotopatrick.onjoue.data.repositories.tag.TagRepository
import com.sotopatrick.onjoue.data.repositories.sampleActivity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.*

internal class IdeaTestActivities : ActivityRepository {
    val rows = MutableStateFlow(listOf(sampleActivity()))
    var failure: Exception? = null
    override fun observeActivities() = flow {
        failure?.let { throw it }
        emitAll(rows)
    }
    override suspend fun getActivity(id: ActivityId) = rows.value.find { it.id == id }
    override suspend fun isTagUsed(id: TagId) = false
    override suspend fun isEquipmentUsed(id: EquipmentId) = false
    override suspend fun save(activity: Activity) = error("Not used")
    override suspend fun deleteActivity(id: ActivityId) = error("Not used")
}

internal class IdeaTestTags : TagRepository {
    val rows = MutableStateFlow(listOf(
        Tag(TagId("inside"), TagGroup.LOCATION, "Dedans"),
        Tag(TagId("outside"), TagGroup.LOCATION, "Dehors"),
    ))
    override fun observeTags(): Flow<List<Tag>> = rows
    override fun observeTagUsage(): Flow<List<TagUsage>> = error("Not used")
    override suspend fun getTag(id: TagId) = rows.value.find { it.id == id }
    override suspend fun save(tag: Tag) = error("Not used")
    override suspend fun deleteTag(id: TagId) = error("Not used")
}

internal class IdeaTestFavorites : FavoriteRepository {
    val rows = MutableStateFlow<List<Favorite>>(emptyList())
    var observationFailure: Exception? = null
    var mutationFailure: Exception? = null
    var gate: CompletableDeferred<Unit>? = null
    var writes = 0
    override fun observeFavorites() = flow {
        observationFailure?.let { throw it }
        emitAll(rows)
    }
    override suspend fun isFavorite(activityId: ActivityId) = rows.value.any { it.activityId == activityId }
    override suspend fun addFavorite(favorite: Favorite) {
        writes++
        gate?.await()
        mutationFailure?.let { throw it }
        rows.value += favorite
    }
    override suspend fun deleteFavorite(id: FavoriteId) {
        writes++
        gate?.await()
        mutationFailure?.let { throw it }
        rows.value = rows.value.filterNot { it.id == id }
    }
}
