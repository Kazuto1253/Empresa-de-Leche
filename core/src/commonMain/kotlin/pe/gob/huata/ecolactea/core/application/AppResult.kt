package pe.gob.huata.ecolactea.core.application

import kotlinx.serialization.Serializable

sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

@Serializable
sealed interface AppError {
    @Serializable
    data class Validation(val fields: Map<String, String>) : AppError

    @Serializable
    data object Unauthorized : AppError

    @Serializable
    data object Forbidden : AppError

    @Serializable
    data object NotFound : AppError

    @Serializable
    data object Offline : AppError

    @Serializable
    data class Conflict(val localVersion: Long?, val serverVersion: Long) : AppError

    @Serializable
    data class Remote(val statusCode: Int, val safeMessage: String) : AppError

    @Serializable
    data class Unexpected(val safeMessage: String) : AppError
}
