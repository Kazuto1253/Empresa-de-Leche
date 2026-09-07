package pe.gob.huata.ecolactea.server

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.routing
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pe.gob.huata.ecolactea.core.application.auth.*
import pe.gob.huata.ecolactea.core.model.*
import pe.gob.huata.ecolactea.core.network.*
import pe.gob.huata.ecolactea.server.auth.authRoutes
import kotlin.test.*

class AuthRoutesTest {
    private fun ApplicationTestBuilder.setup(fixture: AuthFixture) {
        application { configureHttp(); routing { authRoutes(fixture.service) } }
    }
    private suspend fun ApplicationTestBuilder.login(f: AuthFixture, role: Role = Role.PROVEEDOR): HttpResponse =
        client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json); setBody(Json.encodeToString(LoginCommand(role.name, f.password)))
        }
    private suspend fun HttpResponse.session() = Json.decodeFromString<ApiEnvelope<SessionSnapshot>>(bodyAsText()).data!!
    private suspend fun ApplicationTestBuilder.tokenPost(path: String, token: String) = client.post("/api/v1/auth/$path") {
        contentType(ContentType.Application.Json); setBody(Json.encodeToString(RefreshCommand(token)))
    }
    @Test fun allFourRolesLoginWithNoCredentialLeak() = testApplication {
        val f = AuthFixture(); setup(f)
        for (role in Role.entries) {
            val response = login(f, role)
            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            assertFalse(body.contains(f.password)); assertFalse(body.contains(f.hash)); assertFalse(body.contains("password"))
            assertEquals(role, response.session().user.role)
            assertEquals(Authorization.permissions(role), response.session().user.permissions)
            assertEquals("no-store", response.headers[HttpHeaders.CacheControl])
        }
    }
    @Test fun invalidLoginDoesNotRevealAccountExistence() = testApplication {
        val f = AuthFixture(); setup(f)
        val bodies = listOf(Role.PROVEEDOR.name, "nonexistent").map { name ->
            val response = client.post("/api/v1/auth/login") {
                contentType(ContentType.Application.Json); setBody(Json.encodeToString(LoginCommand(name, "incorrect-test-only")))
            }
            assertEquals(HttpStatusCode.Unauthorized, response.status); response.bodyAsText()
        }
        assertEquals(bodies[0], bodies[1])
    }
    @Test fun blockedAccountAndMalformedInputs() = testApplication {
        val f = AuthFixture(); f.block(Role.PROVEEDOR); setup(f)
        assertEquals(HttpStatusCode.Forbidden, login(f).status)
        val malformed = client.post("/api/v1/auth/login") { contentType(ContentType.Application.Json); setBody("{") }
        assertEquals(HttpStatusCode.BadRequest, malformed.status)
        val empty = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json); setBody(Json.encodeToString(LoginCommand("", "")))
        }
        assertEquals(HttpStatusCode.BadRequest, empty.status)
    }
    @Test fun meRequiresSessionAndLogoutImmediatelyRevokesIt() = testApplication {
        val f = AuthFixture(); setup(f)
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/auth/me").status)
        val session = login(f).session()
        assertEquals(HttpStatusCode.OK, client.get("/api/v1/auth/me") { bearerAuth(session.token.accessToken) }.status)
        assertEquals(HttpStatusCode.OK, tokenPost("logout", session.token.refreshToken).status)
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/auth/me") { bearerAuth(session.token.accessToken) }.status)
        assertEquals(HttpStatusCode.Unauthorized, tokenPost("refresh", session.token.refreshToken).status)
        assertEquals(HttpStatusCode.OK, tokenPost("logout", session.token.refreshToken).status)
    }
    @Test fun refreshRotatesAndReplayRevokesFamily() = testApplication {
        val f = AuthFixture(); setup(f)
        val first = login(f).session()
        f.clock.seconds += 901
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/auth/me") { bearerAuth(first.token.accessToken) }.status)
        val refreshed = tokenPost("refresh", first.token.refreshToken)
        assertEquals(HttpStatusCode.OK, refreshed.status)
        val second = refreshed.session()
        assertNotEquals(first.token.refreshToken, second.token.refreshToken)
        assertEquals(HttpStatusCode.Unauthorized, tokenPost("refresh", first.token.refreshToken).status)
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/auth/me") { bearerAuth(second.token.accessToken) }.status)
    }
    @Test fun expiredRefreshAndBlockedAfterLoginAreRejected() = testApplication {
        val f = AuthFixture(); setup(f)
        val first = login(f).session()
        f.clock.seconds += 604800
        val expired = tokenPost("refresh", first.token.refreshToken)
        assertEquals(HttpStatusCode.Unauthorized, expired.status)
        assertTrue(expired.bodyAsText().contains("SESSION_EXPIRED"))
        val second = login(f).session()
        f.block(Role.PROVEEDOR)
        assertEquals(HttpStatusCode.Forbidden, client.get("/api/v1/auth/me") { bearerAuth(second.token.accessToken) }.status)
        assertEquals(HttpStatusCode.Unauthorized, tokenPost("refresh", second.token.refreshToken).status)
    }
    @Test fun permissionsAreEnforcedAndProviderCannotAccessAnotherProvider() = testApplication {
        val f = AuthFixture(); setup(f)
        val admin = login(f, Role.ADMINISTRADOR_GENERAL).session()
        val provider = login(f).session()
        assertEquals(HttpStatusCode.OK, client.get("/api/v1/auth/permissions/MANAGE_USERS") { bearerAuth(admin.token.accessToken) }.status)
        assertEquals(HttpStatusCode.Forbidden, client.get("/api/v1/auth/permissions/MANAGE_USERS") { bearerAuth(provider.token.accessToken) }.status)
        assertEquals(HttpStatusCode.OK, client.get("/api/v1/auth/permissions/PROVIDER_HOME?providerId=provider-own") { bearerAuth(provider.token.accessToken) }.status)
        assertEquals(HttpStatusCode.Forbidden, client.get("/api/v1/auth/permissions/PROVIDER_HOME?providerId=someone-else") { bearerAuth(provider.token.accessToken) }.status)
        assertEquals(HttpStatusCode.Forbidden, client.get("/api/v1/auth/permissions/PROVIDER_HOME") { bearerAuth(provider.token.accessToken) }.status)
    }
}
