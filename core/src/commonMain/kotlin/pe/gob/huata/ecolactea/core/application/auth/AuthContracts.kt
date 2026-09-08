package pe.gob.huata.ecolactea.core.application.auth

import kotlinx.serialization.Serializable
import pe.gob.huata.ecolactea.core.application.AppResult
import pe.gob.huata.ecolactea.core.application.AppError
import pe.gob.huata.ecolactea.core.model.AuthenticatedUser

@Serializable
data class LoginCommand(
    val username: String,
    val password: String,
) {
    override fun toString() = "LoginCommand([REDACTED])"
}

@Serializable
data class SessionToken(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochSeconds: Long,
) {
    override fun toString() = "SessionToken([REDACTED])"
}

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
    data class Unavailable(val error: AppError) : StartupDestination
    data class Authorized(val user: AuthenticatedUser) : StartupDestination
}

class RestoreSessionUseCase(
    private val store: SessionStore,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): StartupDestination {
        val stored = store.read() ?: return StartupDestination.Login

        return when (val validated = authRepository.validate(stored.token.accessToken)) {
            is AppResult.Success -> {
                store.write(stored.copy(user = validated.value))
                StartupDestination.Authorized(validated.value)
            }
            is AppResult.Failure -> when (validated.error) {
                AppError.SessionExpired, AppError.Unauthorized -> refreshOrClear(stored.token.refreshToken)
                AppError.AccountBlocked, AppError.SessionRevoked -> clear()
                else -> StartupDestination.Unavailable(validated.error)
            }
        }
    }

    private suspend fun refreshOrClear(refreshToken: String): StartupDestination =
        when (val refreshed = authRepository.refresh(refreshToken)) {
            is AppResult.Success -> {
                store.write(refreshed.value)
                StartupDestination.Authorized(refreshed.value.user)
            }
            is AppResult.Failure -> {
                when (refreshed.error) {
                    AppError.SessionExpired, AppError.SessionRevoked, AppError.AccountBlocked,
                    AppError.Unauthorized, AppError.InvalidCredentials -> clear()
                    else -> StartupDestination.Unavailable(refreshed.error)
                }
            }
        }

    private suspend fun clear(): StartupDestination {
        store.clear()
        return StartupDestination.Login
    }
}

class LoginUseCase(private val repository: AuthRepository, private val store: SessionStore) {
    suspend operator fun invoke(command: LoginCommand): AppResult<SessionSnapshot> {
        val errors = buildMap {
            if (command.username.isBlank() || command.username.length > 100) put("username", "Ingresa un usuario válido")
            if (command.password.isBlank() || command.password.length > 256) put("password", "Ingresa una contraseña válida")
        }
        if (errors.isNotEmpty()) return AppResult.Failure(AppError.Validation(errors))
        return repository.login(command.copy(username = command.username.trim())).also {
            if (it is AppResult.Success) store.write(it.value)
        }
    }
}

class LogoutUseCase(private val repository: AuthRepository, private val store: SessionStore) {
    suspend operator fun invoke(): AppResult<Unit> {
        // Preserve local logout even if the network is unavailable; report remote failure honestly.
        return try {
            store.read()?.let { repository.logout(it.token.refreshToken) } ?: AppResult.Success(Unit)
        } finally {
            store.clear()
        }
    }
}

/** Safe non-persistent fallback. Never writes credentials or tokens to ordinary files. */
class MemorySessionStore : SessionStore {
    private var session: SessionSnapshot? = null
    override suspend fun read() = session
    override suspend fun write(session: SessionSnapshot) { this.session = session }
    override suspend fun clear() { session = null }
}
