package com.sotopatrick.onjoue.presentation.startup

import androidx.lifecycle.ViewModel
import com.sotopatrick.onjoue.data.repositories.tag.TagRepository
import com.sotopatrick.onjoue.presentation.errors.ErrorParser
import com.sotopatrick.onjoue.presentation.errors.Message
import com.sotopatrick.onjoue.presentation.errors.viewModelAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

data class StartupState(val ready: Boolean = false, val loading: Boolean = false, val error: Message? = null)

class StartupViewModel(
    private val tags: TagRepository,
    private val errorParser: ErrorParser,
) : ViewModel() {
    private val mutableState = MutableStateFlow(StartupState())
    val state = mutableState.asStateFlow()

    init { load() }

    fun load() {
        if (state.value.loading || state.value.ready) return
        mutableState.value = StartupState(loading = true)
        viewModelAction(
            errorParser = errorParser,
            onSuccess = { mutableState.value = StartupState(ready = true) },
            onError = { mutableState.value = StartupState(error = it) },
            action = { tags.observeTags().first() },
        )
    }
}
