package com.sotopatrick.onjoue.presentation.errors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun <T> ViewModel.viewModelAction(
    errorParser: ErrorParser,
    onError: (Message) -> Unit,
    onSuccess: (T) -> Unit = {},
    onPostExecute: () -> Unit = {},
    action: suspend () -> T,
): Job = viewModelScope.launch {
    try {
        val result = try {
            action()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            onError(errorParser.parse(error))
            return@launch
        }
        onSuccess(result)
    } finally {
        onPostExecute()
    }
}
