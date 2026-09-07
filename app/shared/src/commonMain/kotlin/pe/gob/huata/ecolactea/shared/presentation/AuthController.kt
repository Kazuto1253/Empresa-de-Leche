package pe.gob.huata.ecolactea.shared.presentation

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import pe.gob.huata.ecolactea.core.application.*
import pe.gob.huata.ecolactea.core.application.auth.*
import pe.gob.huata.ecolactea.core.model.*

sealed interface AuthState {
    data object Starting : AuthState
    data class Login(val loading: Boolean = false, val error: AppError? = null) : AuthState
    data class Authorized(val user: AuthenticatedUser, val signingOut: Boolean = false) : AuthState
    data class Unavailable(val error: AppError) : AuthState
}

enum class RoleDestination(val title: String) {
    ADMIN("Administrador general"), PLANT("Personal de planta"), COLLECTOR("Acopiador"), PROVIDER("Proveedor");
    companion object {
        fun forRole(role: Role) = when (role) {
            Role.ADMINISTRADOR_GENERAL -> ADMIN
            Role.PERSONAL_PLANTA -> PLANT
            Role.ACOPIADOR -> COLLECTOR
            Role.PROVEEDOR -> PROVIDER
        }
    }
}

class AuthController(private val repository: AuthRepository, private val store: SessionStore) {
    private val mutableState = MutableStateFlow<AuthState>(AuthState.Starting)
    val state = mutableState.asStateFlow()
    private val operation = Mutex()

    suspend fun restore() = exclusive {
        mutableState.value = when (val restored = RestoreSessionUseCase(store, repository)()) {
            StartupDestination.Login -> AuthState.Login()
            is StartupDestination.Authorized -> AuthState.Authorized(restored.user)
            is StartupDestination.Unavailable -> AuthState.Unavailable(restored.error)
        }
    }

    suspend fun login(username: String, password: String) = exclusive {
        if (mutableState.value !is AuthState.Login) return@exclusive
        mutableState.value = AuthState.Login(loading = true)
        mutableState.value = when (val result = LoginUseCase(repository, store)(LoginCommand(username, password))) {
            is AppResult.Success -> AuthState.Authorized(result.value.user)
            is AppResult.Failure -> AuthState.Login(error = result.error)
        }
    }

    suspend fun logout() = exclusive {
        (mutableState.value as? AuthState.Authorized)?.let { mutableState.value = it.copy(signingOut = true) }
        try {
            val result = LogoutUseCase(repository, store)()
            mutableState.value = AuthState.Login(error = if (result is AppResult.Failure)
                AppError.Unexpected("Sesión local cerrada. No se pudo confirmar la revocación remota; la sesión caducará en el servidor.") else null)
        } finally {
            if (mutableState.value !is AuthState.Login) mutableState.value = AuthState.Login()
        }
    }

    private suspend fun exclusive(block: suspend () -> Unit) {
        if (!operation.tryLock()) return
        try { block() }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { mutableState.value = AuthState.Unavailable(AppError.Unexpected("No se pudo acceder al almacenamiento seguro")) }
        finally { operation.unlock() }
    }
}

fun AppError.message(): String = when (this) {
    is AppError.Validation -> "Completa el usuario y la contraseña (máximo 100 y 256 caracteres)."
    AppError.InvalidCredentials -> "Usuario o contraseña incorrectos."
    AppError.AccountBlocked -> "Tu cuenta está bloqueada. Contacta al administrador."
    AppError.SessionExpired, AppError.SessionRevoked, AppError.Unauthorized -> "Tu sesión terminó. Vuelve a ingresar."
    AppError.Forbidden -> "No tienes permiso para esta operación."
    AppError.Offline -> "No se pudo conectar. Revisa tu conexión e inténtalo otra vez."
    is AppError.Unexpected -> safeMessage
    else -> "Servicio no disponible. Inténtalo nuevamente."
}
