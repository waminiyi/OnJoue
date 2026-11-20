package com.sotopatrick.onjoue.presentation.activity.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.models.favorite.Favorite
import com.sotopatrick.onjoue.data.models.favorite.FavoriteId
import com.sotopatrick.onjoue.data.models.tried.Tried
import com.sotopatrick.onjoue.data.models.tried.TriedId
import com.sotopatrick.onjoue.data.repositories.activity.ActivityRepository
import com.sotopatrick.onjoue.data.repositories.favorite.FavoriteRepository
import com.sotopatrick.onjoue.data.repositories.tried.TriedRepository
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

class ActivityDetailsViewModel(
    private val activities: ActivityRepository,
    private val activityId: String,
    private val favorites: FavoriteRepository,
    private val tried: TriedRepository,
    private val errorParser: ErrorParser,
) : ViewModel() {
    private val _state = MutableStateFlow(ActivityDetailsState())
    val state = _state.asStateFlow()
    private val targetId = ActivityId(activityId)
    private var observation: Job? = null

    init {
        load()
        observeFavoritesAndTried()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelAction(
            errorParser = errorParser,
            onSuccess = { activity ->
                _state.update {
                    if (activity == null) {
                        it.copy(
                            activity = null,
                            loading = false,
                            error = Message("Cette idée n’est plus disponible."),
                        )
                    } else {
                        it.copy(activity = activity, loading = false, error = null)
                    }
                }
            },
            onError = { message -> _state.update { it.copy(loading = false, error = message) } },
            action = { activities.getActivity(targetId) },
        )
    }

    private fun observeFavoritesAndTried() {
        observation?.cancel()
        observation = viewModelScope.launch {
            combine(favorites.observeFavorites(), tried.observeTried()) { favs, triedList ->
                val fav = favs.firstOrNull { it.activityId == targetId }
                val tr = triedList.firstOrNull { it.activityId == targetId }
                fav to tr
            }
                .catch { error ->
                    if (error is CancellationException) throw error
                }
                .collect { (fav, tr) ->
                    _state.update {
                        it.copy(
                            isFavorite = fav != null,
                            favoriteId = fav?.id,
                            isTried = tr != null,
                            triedId = tr?.id,
                        )
                    }
                }
        }
    }

    fun toggleFavorite() {
        val current = _state.value
        if (current.busy) return
        val existingId = current.favoriteId
        _state.update { it.copy(busy = true, error = null, notice = null) }
        viewModelAction(
            errorParser = errorParser,
            onError = { message -> _state.update { it.copy(error = message) } },
            onSuccess = {
                _state.update {
                    it.copy(
                        notice = Message(
                            if (existingId == null) "Idée ajoutée aux favoris." else "Idée retirée des favoris.",
                        ),
                    )
                }
            },
            onPostExecute = { _state.update { it.copy(busy = false) } },
        ) {
            if (existingId == null) {
                favorites.addFavorite(Favorite(FavoriteId(Uuid.random().toString()), targetId))
            } else {
                favorites.deleteFavorite(existingId)
            }
        }
    }

    fun toggleTried() {
        val current = _state.value
        if (current.busy) return
        val existingId = current.triedId
        _state.update { it.copy(busy = true, error = null, notice = null) }
        viewModelAction(
            errorParser = errorParser,
            onError = { message -> _state.update { it.copy(error = message) } },
            onSuccess = {
                _state.update {
                    it.copy(
                        notice = Message(
                            if (existingId == null) {
                                "Marquée comme essayée."
                            } else {
                                "Retirée de la liste des idées essayées."
                            },
                        ),
                    )
                }
            },
            onPostExecute = { _state.update { it.copy(busy = false) } },
        ) {
            if (existingId == null) {
                tried.addTried(Tried(TriedId(Uuid.random().toString()), targetId))
            } else {
                tried.deleteTried(existingId)
            }
        }
    }

    fun clearNotice() {
        _state.update { it.copy(notice = null) }
    }
}
