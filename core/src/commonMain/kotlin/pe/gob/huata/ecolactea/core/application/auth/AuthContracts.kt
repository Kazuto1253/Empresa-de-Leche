package pe.gob.huata.ecolactea.core.application.auth

import kotlinx.serialization.Serializable
import pe.gob.huata.ecolactea.core.application.AppResult
import pe.gob.huata.ecolactea.core.model.AuthenticatedUser

data class LoginCommand(
    val username: String,
    val password: String,
)

@Serializable
data class SessionToken(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochSeconds: Long,
)

@Serializable
data class SessionSnapshot(
    val user: AuthenticatedUser,
    val token: SessionToken,
)

interface AuthRepository {
    suspend fun login(command: LoginCommand): AppResult<SessionSnapshot>
    suspend fun refresh(refreshToken: String): AppResult<SessionSnapshot>
    suspend fun validate(accessToken: String): AppResult<AuthenticatedUser>
    suspend fun logout(refreshToken: String): AppResult<Unit>
}

interface SessionStore {
    suspend fun read(): SessionSnapshot?
    suspend fun write(session: SessionSnapshot)
    suspend fun clear()
}

interface SessionManager {
    suspend fun restore(): StartupDestination
    suspend fun currentUser(): AuthenticatedUser?
    suspend fun signOut()
}

sealed interface StartupDestination {
    data object Login : StartupDestination
    data class Authorized(val user: AuthenticatedUser) : StartupDestination
}

class RestoreSessionUseCase(
    private val store: SessionStore,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): StartupDestination {
        val stored = store.read() ?: return StartupDestination.Login

        return when (val validated = authRepository.validate(stored.token.accessToken)) {
            is AppResult.Success -> StartupDestination.Authorized(validated.value)
            is AppResult.Failure -> refreshOrClear(stored.token.refreshToken)
        }
    }

    private suspend fun refreshOrClear(refreshToken: String): StartupDestination =
        when (val refreshed = authRepository.refresh(refreshToken)) {
            is AppResult.Success -> {
                store.write(refreshed.value)
                StartupDestination.Authorized(refreshed.value.user)
            }
            is AppResult.Failure -> {
                store.clear()
                StartupDestination.Login
            }
        }
}
