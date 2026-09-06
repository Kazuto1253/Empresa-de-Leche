package pe.gob.huata.ecolactea.server

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.json.Json
import pe.gob.huata.ecolactea.core.network.ApiEnvelope
import pe.gob.huata.ecolactea.core.network.ApiErrorDto

fun Application.configureHttp() {
    install(CallLogging)
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
        exception<Throwable> { call, cause ->
            this@configureHttp.environment.log.error("Unhandled request error", cause)
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
