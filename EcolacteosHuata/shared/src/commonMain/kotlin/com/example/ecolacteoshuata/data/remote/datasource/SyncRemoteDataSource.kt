package com.example.ecolacteoshuata.data.remote.datasource

import com.example.ecolacteoshuata.data.remote.ApiConfig
import com.example.ecolacteoshuata.data.remote.dto.CambioDto
import com.example.ecolacteoshuata.data.remote.dto.SubidaCambiosRequest
import com.example.ecolacteoshuata.data.remote.dto.SubidaCambiosResponse
import com.example.ecolacteoshuata.data.remote.dto.SyncResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlin.time.TimeSource

/**
 * `GET /api/mobile/sync` y `POST /api/mobile/cambios` del backend Laravel (MySQL). El token lo
 * agrega el plugin Auth del HttpClient (ver HttpClientFactory).
 */
class SyncRemoteDataSource(
    private val client: HttpClient,
    private val config: ApiConfig,
) {
    suspend fun sync(host: String, puerto: String, since: String?): SyncResponseDto {
        val respuesta: HttpResponse = client.get(config.baseUrl(host, puerto) + "sync") {
            if (!since.isNullOrBlank()) parameter("since", since)
        }
        if (respuesta.status == HttpStatusCode.Unauthorized) throw TokenRechazadoException()
        check(respuesta.status.isSuccess()) { "El servidor respondió ${respuesta.status}." }
        return respuesta.body()
    }

    /** Sube lo que se creó, editó o eliminó en el teléfono, en el orden en que pasó. */
    suspend fun subirCambios(host: String, puerto: String, cambios: List<CambioDto>): SubidaCambiosResponse {
        val respuesta: HttpResponse = client.post(config.baseUrl(host, puerto) + "cambios") {
            contentType(ContentType.Application.Json)
            setBody(SubidaCambiosRequest(cambios))
        }
        if (respuesta.status == HttpStatusCode.Unauthorized) throw TokenRechazadoException()
        check(respuesta.status.isSuccess()) { "El servidor respondió ${respuesta.status} al subir los cambios." }
        return respuesta.body()
    }

    /**
     * Comprueba que el servidor responda y devuelve cuántos milisegundos tardó. Un 401 de
     * `/api/mobile/sync` también confirma que es el Laravel de la app y está encendido.
     */
    suspend fun probar(host: String, puerto: String): Long {
        val inicio = TimeSource.Monotonic.markNow()
        val respuesta: HttpResponse = client.get(config.baseUrl(host, puerto) + "sync") {
            parameter("since", "2999-01-01T00:00:00+00:00")
        }
        check(respuesta.status == HttpStatusCode.Unauthorized || respuesta.status.isSuccess()) {
            "El servidor respondió ${respuesta.status}."
        }
        return inicio.elapsedNow().inWholeMilliseconds
    }
}

/** Laravel no aceptó el token (nunca se inició sesión, se desactivó el usuario o cambió el servidor). */
class TokenRechazadoException : Exception("El servidor pide iniciar sesión nuevamente.")
