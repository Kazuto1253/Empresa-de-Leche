package pe.gob.huata.ecolactea.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import pe.gob.huata.ecolactea.core.application.*
import pe.gob.huata.ecolactea.core.application.auth.*
import pe.gob.huata.ecolactea.core.model.AuthenticatedUser
import pe.gob.huata.ecolactea.core.network.*

class HttpAuthRepository(private val client: HttpClient) : AuthRepository {
    override suspend fun login(command: LoginCommand): AppResult<SessionSnapshot> = request {
        client.post("/api/v1/auth/login") { contentType(ContentType.Application.Json); setBody(command) }
    }
    override suspend fun refresh(refreshToken: String): AppResult<SessionSnapshot> = request {
        client.post("/api/v1/auth/refresh") { contentType(ContentType.Application.Json); setBody(RefreshCommand(refreshToken)) }
    }
    override suspend fun validate(accessToken: String): AppResult<AuthenticatedUser> = request {
        client.get("/api/v1/auth/me") { bearerAuth(accessToken) }
    }
    override suspend fun logout(refreshToken: String): AppResult<Unit> = request {
        client.post("/api/v1/auth/logout") { contentType(ContentType.Application.Json); setBody(RefreshCommand(refreshToken)) }
    }

    private suspend inline fun <reified T> request(block: () -> HttpResponse): AppResult<T> = try {
        val response = block()
        if (response.status.isSuccess()) {
            response.body<ApiEnvelope<T>>().data?.let { AppResult.Success(it) }
                ?: AppResult.Failure(AppError.Unexpected("Respuesta inválida del servidor"))
        } else {
            val error = response.body<ApiEnvelope<Unit>>().error
            AppResult.Failure(when (error?.code) {
                "INVALID_CREDENTIALS" -> AppError.InvalidCredentials
                "ACCOUNT_BLOCKED" -> AppError.AccountBlocked
                "SESSION_EXPIRED" -> AppError.SessionExpired
                "SESSION_REVOKED" -> AppError.SessionRevoked
                "UNAUTHORIZED" -> AppError.Unauthorized
                "FORBIDDEN" -> AppError.Forbidden
                "VALIDATION" -> AppError.Validation(error.fieldErrors)
                else -> AppError.Remote(response.status.value, "Servicio no disponible")
            })
        }
    } catch (cancelled: CancellationException) { throw cancelled }
    catch (_: SerializationException) { AppResult.Failure(AppError.Unexpected("Respuesta inválida del servidor")) }
    catch (_: Exception) { AppResult.Failure(AppError.Offline) }
}
