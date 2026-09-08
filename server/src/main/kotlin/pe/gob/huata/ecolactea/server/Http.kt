package pe.gob.huata.ecolactea.server

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.response.respond
import io.ktor.server.request.httpMethod
import kotlinx.serialization.json.Json
import pe.gob.huata.ecolactea.core.network.ApiEnvelope
import pe.gob.huata.ecolactea.core.network.ApiErrorDto

fun Application.configureHttp() {
    install(CORS) {
        allowHost("127.0.0.1:8081", schemes = listOf("http"))
        allowHost("localhost:8081", schemes = listOf("http"))
        allowHost("127.0.0.1:8082", schemes = listOf("http"))
        allowHost("localhost:8082", schemes = listOf("http"))
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
    }
    install(CallLogging) {
        // Do not log URLs/query strings, headers, payloads or exception messages.
        format { call -> "${call.request.httpMethod.value} ${call.response.status()?.value ?: 0}" }
    }
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            },
        )
    }
    install(StatusPages) {
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ApiEnvelope<Unit>(error = ApiErrorDto("VALIDATION", "Solicitud inválida")))
        }
        exception<Throwable> { call, cause ->
            // Exception messages can contain SQL or user input. Never log their contents.
            this@configureHttp.environment.log.error("Unhandled request error: {}", cause::class.simpleName)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiEnvelope<Unit>(
                    error = ApiErrorDto(
                        code = "UNEXPECTED",
                        message = "Ocurrió un error inesperado",
                    ),
                ),
            )
        }
    }
}
