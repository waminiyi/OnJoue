package com.sotopatrick.onjoue.data.sources.room

import androidx.room.Transactor
import androidx.room.useWriterConnection
import androidx.sqlite.SQLiteException
import com.sotopatrick.onjoue.data.models.common.AppException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

internal enum class DatabaseOperation { READ, INSERT, UPDATE, DELETE }

internal fun mapDatabaseException(error: Exception, operation: DatabaseOperation): Exception {
    if (error is CancellationException || error is AppException) return error
    if (error !is SQLiteException) return error
    // SQLiteException in the KMP API exposes no numeric error-code property.
    val detail = error.message.orEmpty()
    return when {
        detail.contains("UNIQUE constraint failed", true) ||
            detail.contains("PRIMARY KEY", true) ||
            Regex("(?i)\\bcode[:= ]+(1555|2067)\\b").containsMatchIn(detail) -> AppException.Conflict(error)
        detail.contains("FOREIGN KEY constraint failed", true) ||
            Regex("(?i)\\bcode[:= ]+787\\b").containsMatchIn(detail) ->
            if (operation == DatabaseOperation.DELETE) AppException.InUse(error)
            else AppException.InvalidReference(error)
        else -> AppException.Storage(error)
    }
}

internal suspend fun <T> databaseAccess(
    operation: DatabaseOperation = DatabaseOperation.READ,
    action: suspend () -> T,
): T = try {
    action()
} catch (error: Exception) {
    throw mapDatabaseException(error, operation)
}

internal fun <T> Flow<T>.mapStorageErrors(): Flow<T> = catch { error ->
    if (error is Exception) throw mapDatabaseException(error, DatabaseOperation.READ)
    throw error
}

internal inline fun <T> decodeStoredData(action: () -> T): T = try {
    action()
} catch (error: IllegalArgumentException) {
    throw AppException.CorruptData(error)
}

internal suspend fun <T> AppDatabase.transaction(
    operation: DatabaseOperation,
    action: suspend () -> T,
): T = databaseAccess(operation) {
    useWriterConnection { connection ->
        connection.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) { action() }
    }
}
