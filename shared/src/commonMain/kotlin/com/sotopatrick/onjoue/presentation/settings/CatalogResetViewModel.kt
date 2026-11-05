package com.sotopatrick.onjoue.presentation.settings

import androidx.lifecycle.ViewModel
import com.sotopatrick.onjoue.presentation.errors.ErrorParser
import com.sotopatrick.onjoue.presentation.errors.Message
import com.sotopatrick.onjoue.presentation.errors.viewModelAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CatalogResetState(
    val busy: Boolean = false,
    val message: Message? = null,
    val succeeded: Boolean = false,
)

class CatalogResetViewModel(
    private val errorParser: ErrorParser,
    private val replaceCatalog: suspend () -> Unit,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CatalogResetState())
    val state = mutableState.asStateFlow()

    fun reset() {
        if (state.value.busy) return
        mutableState.value = CatalogResetState(busy = true)
        viewModelAction(
            errorParser = errorParser,
            onError = { mutableState.value = CatalogResetState(message = it) },
            onSuccess = {
                mutableState.value = CatalogResetState(message = Message("Catalogue remplacé."), succeeded = true)
            },
            action = { replaceCatalog() },
        )
    }
}
