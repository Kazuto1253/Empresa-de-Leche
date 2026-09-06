package pe.gob.huata.ecolactea.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.URLProtocol
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pe.gob.huata.ecolactea.shared.config.AppEnvironment

fun createApiClient(environment: AppEnvironment): HttpClient =
    HttpClient {
        install(ContentNegotiation) {
            json(ecolacteaJson)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 15_000
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        defaultRequest {
            url.takeFrom(environment.apiBaseUrl)
            if (url.protocol == URLProtocol.HTTP || url.protocol == URLProtocol.HTTPS) {
                // The backend path is intentionally left to repositories/use cases.
            }
        }
    }

val ecolacteaJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
}
