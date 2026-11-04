package com.sotopatrick.onjoue.data.models.common


enum class ValidationReason { EMPTY_LABEL, INVALID_HIERARCHY, INVALID_ACTIVITY }

sealed class AppException(cause: Throwable? = null) : Exception(cause) {
    class Validation(val reason: ValidationReason) : AppException()
    class Conflict(cause: Throwable? = null) : AppException(cause)
    class NotFound : AppException()
    class InUse(cause: Throwable? = null) : AppException(cause)
    class Forbidden : AppException()
    class InvalidReference(cause: Throwable? = null) : AppException(cause)
    class CorruptData(cause: Throwable) : AppException(cause)
    class Storage(cause: Throwable) : AppException(cause)
}
