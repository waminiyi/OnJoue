@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.sotopatrick.onjoue.presentation.errors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import com.sotopatrick.onjoue.data.models.common.AppException
import com.sotopatrick.onjoue.data.models.common.ValidationReason
import com.sotopatrick.onjoue.data.sources.room.DatabaseOperation
import com.sotopatrick.onjoue.data.sources.room.mapDatabaseException
import com.sotopatrick.onjoue.data.sources.room.mapStorageErrors
import kotlin.test.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*

class ErrorHandlingTest {
    private val dispatcher = StandardTestDispatcher()
    private class TestViewModel : ViewModel()
    private class ScopedViewModel(scope: CoroutineScope) : ViewModel(scope)
    @BeforeTest
    fun setUp() { Dispatchers.setMain(dispatcher) }
    @AfterTest
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun parserMapsTypedErrorsWithoutExposingTechnicalDetails() {
        val parser = FrenchErrorParser()
        val cause = IllegalStateException("private file path")
        val errors = listOf(
            AppException.Validation(ValidationReason.EMPTY_LABEL),
            AppException.Validation(ValidationReason.INVALID_HIERARCHY),
            AppException.Validation(ValidationReason.INVALID_ACTIVITY),
            AppException.Conflict(cause), AppException.NotFound(), AppException.InUse(cause),
            AppException.Forbidden(), AppException.InvalidReference(cause),
            AppException.CorruptData(cause), AppException.Storage(cause), cause,
        )
        for (error in errors) {
            val message = parser.parse(error)
            assertTrue(message.content.isNotBlank())
            assertFalse(message.content.contains("private"))
        }
        assertFailsWith<CancellationException> { parser.parse(CancellationException()) }
        val typed = AppException.Conflict(cause)
        assertSame(typed, mapDatabaseException(typed, DatabaseOperation.INSERT))
        assertSame(cause, typed.cause)
        assertSame(cause, mapDatabaseException(cause, DatabaseOperation.READ))
    }

    @Test
    fun suspendActionReportsSuccessThenCleanup() = runTest(dispatcher) {
        val events = mutableListOf<String>()
        val job = TestViewModel().viewModelAction(
            errorParser = FrenchErrorParser(),
            onError = { fail("Unexpected error") },
            onSuccess = { events += "success:$it" },
            onPostExecute = { events += "cleanup" },
        ) { testScheduler.runCurrent(); 42 }
        job.join()
        assertEquals(listOf("success:42", "cleanup"), events)
    }

    @Test
    fun failureIsParsedOnceAndAlwaysCleansUp() = runTest(dispatcher) {
        val exception = AppException.NotFound()
        var parsed = 0
        val events = mutableListOf<String>()
        TestViewModel().viewModelAction(
            errorParser = ErrorParser { assertSame(exception, it); parsed++; Message("missing") },
            onError = { events += it.content },
            onSuccess = { fail("Unexpected success") },
            onPostExecute = { events += "cleanup" },
        ) { throw exception }.join()
        assertEquals(1, parsed)
        assertEquals(listOf("missing", "cleanup"), events)
    }

    @Test
    fun cancellationIsNotParsedAndStillCleansUp() = runTest(dispatcher) {
        var cleaned = false
        val job = TestViewModel().viewModelAction(
            errorParser = ErrorParser { fail("Cancellation must not be parsed") },
            onError = { fail("Cancellation must not become a UI error") },
            onSuccess = { fail("Unexpected success") },
            onPostExecute = { cleaned = true },
        ) { awaitCancellation() }
        runCurrent()
        job.cancel()
        job.join()
        assertTrue(job.isCancelled)
        assertTrue(cleaned)
    }

    @Test
    fun clearingViewModelCancelsActionWithoutUiError() = runTest(dispatcher) {
        val store = ViewModelStore()
        val vm = TestViewModel()
        store.put("test", vm)
        var cleaned = false
        val job = vm.viewModelAction(
            errorParser = ErrorParser { fail("Cancellation must not be parsed") },
            onError = { fail("Unexpected error") },
            onPostExecute = { cleaned = true },
        ) { awaitCancellation() }
        runCurrent()
        store.clear()
        job.join()
        assertTrue(job.isCancelled)
        assertTrue(cleaned)
    }

    @Test
    fun flowTranslationPreservesCancellationAndDoesNotCatchConsumerErrors() = runTest {
        val cancelled = CancellationException("cancel")
        val thrown = assertFailsWith<CancellationException> {
            flow<Int> { throw cancelled }.mapStorageErrors().collect()
        }
        assertSame(cancelled, thrown)
        val consumerError = IllegalStateException("consumer")
        val consumerThrown = assertFailsWith<IllegalStateException> {
            flow { emit(1) }.mapStorageErrors().collect { throw consumerError }
        }
        assertSame(consumerError, consumerThrown)
    }

    @Test
    fun successCallbackErrorsAreNotParsedAsActionFailures() = runTest(dispatcher) {
        val callbackError = IllegalStateException("callback")
        var reported: Throwable? = null
        var cleaned = false
        val scope = CoroutineScope(SupervisorJob() + dispatcher + CoroutineExceptionHandler { _, error -> reported = error })
        try {
            ScopedViewModel(scope).viewModelAction(
                errorParser = ErrorParser { fail("Callback errors must not be parsed") },
                onError = { fail("Unexpected action error") },
                onSuccess = { throw callbackError },
                onPostExecute = { cleaned = true },
            ) { 42 }.join()
            assertSame(callbackError, reported)
            assertTrue(cleaned)
        } finally { scope.cancel() }
    }
}
