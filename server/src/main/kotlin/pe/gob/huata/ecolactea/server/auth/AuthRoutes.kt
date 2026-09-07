package pe.gob.huata.ecolactea.server.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pe.gob.huata.ecolactea.core.application.*
import pe.gob.huata.ecolactea.core.application.auth.LoginCommand
import pe.gob.huata.ecolactea.core.model.Permission
import pe.gob.huata.ecolactea.core.network.*

fun Route.authRoutes(service: AuthService?) {
    route("/api/v1/auth") {
        post("/login") {
            val command = call.receive<LoginCommand>()
            call.authResponse(service) { login(command) }
        }
        post("/refresh") {
            val command = call.receive<RefreshCommand>()
            call.authResponse(service) { refresh(command.refreshToken) }
        }
        post("/logout") {
            val command = call.receive<RefreshCommand>()
            call.authResponse(service) { logout(command.refreshToken) }
        }
        get("/me") { call.authResponse(service) { me(call.bearer()) } }
        // Minimal protected capability check; no business/admin functionality is implemented here.
        get("/permissions/{permission}") {
            val permission = Permission.entries.find { it.name == call.parameters["permission"] }
            call.authResponse(service) {
                if (permission == null) AppResult.Failure(AppError.Forbidden)
                else authorize(call.bearer(), permission, call.request.queryParameters["providerId"])
            }
        }
    }
}

private fun ApplicationCall.bearer(): String = request.headers[HttpHeaders.Authorization]
    ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }?.substring(7).orEmpty()

private suspend inline fun <reified T : Any> ApplicationCall.authResponse(
    service: AuthService?, crossinline action: AuthService.() -> AppResult<T>,
) {
    response.header(HttpHeaders.CacheControl, "no-store")
    response.header(HttpHeaders.Pragma, "no-cache")
    if (service == null) {
        respond(HttpStatusCode.ServiceUnavailable, ApiEnvelope<Unit>(error = ApiErrorDto("UNAVAILABLE", "Servicio no disponible")))
        return
    }
    when (val result = withContext(Dispatchers.IO) { service.action() }) {
        is AppResult.Success -> respond(ApiEnvelope(data = result.value))
        is AppResult.Failure -> {
            val (status, code, message) = when (result.error) {
                is AppError.Validation -> Triple(HttpStatusCode.BadRequest, "VALIDATION", "Completa usuario y contraseña")
                AppError.InvalidCredentials -> Triple(HttpStatusCode.Unauthorized, "INVALID_CREDENTIALS", "Usuario o contraseña incorrectos")
                AppError.AccountBlocked -> Triple(HttpStatusCode.Forbidden, "ACCOUNT_BLOCKED", "Cuenta bloqueada. Contacta al administrador")
                AppError.SessionExpired -> Triple(HttpStatusCode.Unauthorized, "SESSION_EXPIRED", "La sesión expiró")
                AppError.SessionRevoked -> Triple(HttpStatusCode.Unauthorized, "SESSION_REVOKED", "La sesión fue revocada")
                AppError.Forbidden -> Triple(HttpStatusCode.Forbidden, "FORBIDDEN", "No tienes permiso para esta operación")
                else -> Triple(HttpStatusCode.Unauthorized, "UNAUTHORIZED", "Inicia sesión para continuar")
            }
            respond(status, ApiEnvelope<Unit>(error = ApiErrorDto(code, message)))
        }
    }
}
