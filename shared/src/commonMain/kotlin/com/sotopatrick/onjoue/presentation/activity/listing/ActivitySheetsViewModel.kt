package com.sotopatrick.onjoue.presentation.activity.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.models.favorite.Favorite
import com.sotopatrick.onjoue.data.models.favorite.FavoriteId
import com.sotopatrick.onjoue.data.models.tag.TagGroup
import com.sotopatrick.onjoue.data.repositories.activity.ActivityRepository
import com.sotopatrick.onjoue.data.repositories.favorite.FavoriteRepository
import com.sotopatrick.onjoue.data.repositories.tag.TagRepository
import com.sotopatrick.onjoue.presentation.activity.favorites.IdeaFavoritesState
import com.sotopatrick.onjoue.presentation.errors.ErrorParser
import com.sotopatrick.onjoue.presentation.errors.Message
import com.sotopatrick.onjoue.presentation.errors.viewModelAction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class ActivitySheetsViewModel(
    private val activities: ActivityRepository,
    private val errorParser: ErrorParser,
    private val favorites: FavoriteRepository,
    private val tags: TagRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ActivitySheetsState())
    val state = _state.asStateFlow()
    private var observation: Job? = null
    private var favoriteObservation: Job? = null

    init {
        load()
        loadFavorites()
    }

    fun load() {
        observation?.cancel()
        _state.update { it.copy(loading = true, error = null) }
        observation = viewModelScope.launch {
            combine(activities.observeActivities(), tags.observeTags()) { rows, catalog ->
                rows to catalog.filter {
                    it.group == TagGroup.LOCATION && it.parent == null &&
                        it.label.trim().lowercase() in QuickLocationLabels
                }
            }
                .catch { error ->
                    if (error is CancellationException) throw error
                    if (error !is Exception) throw error
                    _state.update { it.copy(loading = false, error = errorParser.parse(error)) }
                }
                .collect { (rows, locations) ->
                    _state.update { it.copy(activities = rows, locations = locations, loading = false, error = null) }
                }
        }
    }

    fun loadFavorites() {
        favoriteObservation?.cancel()
        updateFavorites { it.copy(ready = false, error = null) }
        favoriteObservation = viewModelScope.launch {
            favorites.observeFavorites()
                .catch { error ->
                    if (error is CancellationException) throw error
                    if (error !is Exception) throw error
                    updateFavorites { it.copy(ready = false, error = errorParser.parse(error)) }
                }
                .collect { rows ->
                    updateFavorites { it.copy(entries = rows.associateBy { row -> row.activityId }, ready = true) }
                }
        }
    }

    fun toggleFavorite(activityId: String) {
        val id = ActivityId(activityId)
        val current = _state.value
        if (!current.favorites.ready || current.favorites.busy != null ||
            current.activities.none { it.id == id }
        ) return
        val existing = current.favorites.entries[id]
        val favorite = existing ?: Favorite(FavoriteId(Uuid.random().toString()), id)
        updateFavorites { it.copy(busy = id, error = null, notice = null) }
        viewModelAction(
            errorParser = errorParser,
            onError = { message -> updateFavorites { it.copy(error = message) } },
            onSuccess = {
                updateFavorites {
                    it.copy(
                        entries = if (existing == null) it.entries + (id to favorite) else it.entries - id,
                        notice = Message(
                            if (existing == null) "Idée ajoutée aux favoris." else "Idée retirée des favoris.",
                        ),
                    )
                }
            },
            onPostExecute = { updateFavorites { it.copy(busy = null) } },
        ) {
            if (existing == null) favorites.addFavorite(favorite) else favorites.deleteFavorite(existing.id)
        }
    }

    fun clearNotice() {
        updateFavorites { it.copy(notice = null) }
    }

    private fun updateFavorites(transform: (IdeaFavoritesState) -> IdeaFavoritesState) {
        _state.update { it.copy(favorites = transform(it.favorites)) }
    }
}

private val QuickLocationLabels = setOf("dedans", "dehors")
