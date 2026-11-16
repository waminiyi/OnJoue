@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.sotopatrick.onjoue.presentation.activity

import androidx.lifecycle.ViewModelStore
import com.sotopatrick.onjoue.data.models.common.AppException
import com.sotopatrick.onjoue.data.models.favorite.*
import com.sotopatrick.onjoue.presentation.errors.FrenchErrorParser
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import kotlin.test.*

class ActivitySheetsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val activities = IdeaTestActivities()
    private val favorites = IdeaTestFavorites()
    private val tags = IdeaTestTags()

    @BeforeTest
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @AfterTest
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    private fun viewModel() = ActivitySheetsViewModel(activities, FrenchErrorParser(), favorites, tags).also {
        store.put("listing", it)
    }

    @Test
    fun loadsCatalogAndObservesExistingFavorites() = runTest(dispatcher) {
        val activity = activities.rows.value.single()
        val favorite = Favorite(FavoriteId("saved"), activity.id)
        favorites.rows.value = listOf(favorite)
        val vm = viewModel()
        assertTrue(vm.state.value.loading)
        runCurrent()
        assertFalse(vm.state.value.loading)
        assertEquals(activities.rows.value, vm.state.value.activities)
        assertEquals(tags.rows.value, vm.state.value.locations)
        assertEquals(favorite, vm.state.value.favorites.entries[activity.id])
        assertTrue(vm.state.value.favorites.ready)
        favorites.rows.value = emptyList()
        runCurrent()
        assertTrue(vm.state.value.favorites.entries.isEmpty())
    }

    @Test
    fun addsAndRemovesFavoritesWithStableStoredIdentifier() = runTest(dispatcher) {
        val vm = viewModel()
        runCurrent()
        val id = activities.rows.value.single().id
        vm.toggleFavorite(id.id)
        runCurrent()
        val added = favorites.rows.value.single()
        assertEquals(id, added.activityId)
        assertEquals(added, vm.state.value.favorites.entries[id])
        assertEquals("Idée ajoutée aux favoris.", vm.state.value.favorites.notice?.content)
        vm.toggleFavorite(id.id)
        runCurrent()
        assertTrue(favorites.rows.value.isEmpty())
        assertTrue(vm.state.value.favorites.entries.isEmpty())
        assertEquals("Idée retirée des favoris.", vm.state.value.favorites.notice?.content)
        assertEquals(2, favorites.writes)
    }

    @Test
    fun repeatedClicksCannotStartConcurrentWrites() = runTest(dispatcher) {
        favorites.gate = CompletableDeferred()
        val vm = viewModel()
        runCurrent()
        val id = activities.rows.value.single().id
        vm.toggleFavorite(id.id)
        vm.toggleFavorite(id.id)
        runCurrent()
        assertEquals(1, favorites.writes)
        assertEquals(id, vm.state.value.favorites.busy)
        favorites.gate!!.complete(Unit)
        runCurrent()
        assertNull(vm.state.value.favorites.busy)
        assertEquals(1, favorites.rows.value.size)
    }

    @Test
    fun mutationFailurePreservesStateAndAllowsRetry() = runTest(dispatcher) {
        favorites.mutationFailure = AppException.NotFound()
        val vm = viewModel()
        runCurrent()
        val id = activities.rows.value.single().id
        vm.toggleFavorite(id.id)
        runCurrent()
        assertNotNull(vm.state.value.favorites.error)
        assertNull(vm.state.value.favorites.busy)
        assertTrue(vm.state.value.favorites.entries.isEmpty())
        assertNull(vm.state.value.favorites.notice)
        favorites.mutationFailure = null
        vm.toggleFavorite(id.id)
        runCurrent()
        assertNull(vm.state.value.favorites.error)
        assertEquals(1, favorites.rows.value.size)
    }

    @Test
    fun observationFailureDoesNotHideIdeasAndDisablesFavoriteWritesUntilRetry() = runTest(dispatcher) {
        favorites.observationFailure = AppException.NotFound()
        val vm = viewModel()
        runCurrent()
        assertFalse(vm.state.value.loading)
        assertNull(vm.state.value.error)
        assertNotNull(vm.state.value.favorites.error)
        assertFalse(vm.state.value.favorites.ready)
        vm.toggleFavorite(activities.rows.value.single().id.id)
        runCurrent()
        assertEquals(0, favorites.writes)
        favorites.observationFailure = null
        vm.loadFavorites()
        runCurrent()
        assertTrue(vm.state.value.favorites.ready)
        assertNull(vm.state.value.favorites.error)
    }

    @Test
    fun catalogErrorsCanBeRetriedWithoutClearingPreviouslyLoadedIdeas() = runTest(dispatcher) {
        val vm = viewModel()
        runCurrent()
        activities.failure = AppException.NotFound()
        vm.load()
        runCurrent()
        assertEquals(activities.rows.value, vm.state.value.activities)
        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.loading)
        activities.failure = null
        vm.load()
        runCurrent()
        assertNull(vm.state.value.error)
        assertFalse(vm.state.value.loading)
    }

    @Test
    fun clearingViewModelCancelsPendingWriteAndCleansBusyState() = runTest(dispatcher) {
        favorites.gate = CompletableDeferred()
        val vm = viewModel()
        runCurrent()
        vm.toggleFavorite(activities.rows.value.single().id.id)
        runCurrent()
        store.clear()
        runCurrent()
        assertNull(vm.state.value.favorites.busy)
        assertNull(vm.state.value.favorites.error)
        assertTrue(favorites.rows.value.isEmpty())
    }
}
