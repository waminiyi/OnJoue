package com.sotopatrick.onjoue.presentation.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sotopatrick.onjoue.data.models.activity.Activity
import com.sotopatrick.onjoue.data.models.activity.ActivityId
import com.sotopatrick.onjoue.data.repositories.activity.ActivityRepository
import com.sotopatrick.onjoue.presentation.errors.ErrorParser
import com.sotopatrick.onjoue.presentation.errors.Message
import com.sotopatrick.onjoue.presentation.errors.viewModelAction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActivitySheetsState(
    val activities: List<Activity> = emptyList(),
    val loading: Boolean = true,
    val error: Message? = null,
)

data class ActivityDetailsState(
    val activity: Activity? = null,
    val loading: Boolean = true,
    val error: Message? = null,
)

class ActivitySheetsViewModel(
    private val activities: ActivityRepository,
    private val errorParser: ErrorParser,
) : ViewModel() {
    private val _state = MutableStateFlow(ActivitySheetsState())
    val state = _state.asStateFlow()
    private var observation: Job? = null

    init {
        load()
    }

    fun load() {
        observation?.cancel()
        _state.update { it.copy(loading = true, error = null) }
        observation = viewModelScope.launch {
            activities.observeActivities()
                .catch { error ->
                    if (error is CancellationException) throw error
                    if (error !is Exception) throw error
                    _state.update { it.copy(loading = false, error = errorParser.parse(error)) }
                }
                .collect { rows -> _state.update { it.copy(activities = rows, loading = false, error = null) } }
        }
    }
}

class ActivityDetailsViewModel(
    private val activities: ActivityRepository,
    private val activityId: String,
    private val errorParser: ErrorParser,
) : ViewModel() {
    private val _state = MutableStateFlow(ActivityDetailsState())
    val state = _state.asStateFlow()

    init {
        load()
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
                            error = Message("Cette fiche n’est plus disponible."),
                        )
                    } else it.copy(activity = activity, loading = false, error = null)
                }
            },
            onError = { message -> _state.update { it.copy(loading = false, error = message) } },
            action = { activities.getActivity(ActivityId(activityId)) },
        )
    }
}
