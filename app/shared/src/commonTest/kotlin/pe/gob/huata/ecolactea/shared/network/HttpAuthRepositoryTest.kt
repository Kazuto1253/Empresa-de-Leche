package pe.gob.huata.ecolactea.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import pe.gob.huata.ecolactea.core.application.*
import pe.gob.huata.ecolactea.core.application.auth.LoginCommand
import kotlin.test.*

class HttpAuthRepositoryTest {
    @Test fun typedErrorsAreMappedWithoutExposingServerMessages() = runTest {
        val client = HttpClient(MockEngine {
            assertEquals("/api/v1/auth/login", it.url.encodedPath)
            respond("""{"error":{"code":"INVALID_CREDENTIALS","message":"ignored"}}""",
                HttpStatusCode.Unauthorized, headersOf(HttpHeaders.ContentType, "application/json"))
        }) { install(ContentNegotiation) { json(ecolacteaJson) } }
        try { assertEquals(AppResult.Failure(AppError.InvalidCredentials), HttpAuthRepository(client).login(LoginCommand("test", "test-password"))) }
        finally { client.close() }
    }
    @Test fun unknownBackendRoleFailsClosed() = runTest {
        val client = HttpClient(MockEngine {
            respond("""{"data":{"userId":"test","username":"test","role":"SUPERUSER"}}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }) { install(ContentNegotiation) { json(ecolacteaJson) } }
        try { assertIs<AppResult.Failure>(HttpAuthRepository(client).validate("test-access")) }
        finally { client.close() }
    }
}
