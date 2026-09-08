package pe.gob.huata.ecolactea.server

import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import pe.gob.huata.ecolactea.core.application.*
import pe.gob.huata.ecolactea.core.application.auth.*
import pe.gob.huata.ecolactea.core.model.Role
import pe.gob.huata.ecolactea.server.auth.*
import pe.gob.huata.ecolactea.shared.network.HttpAuthRepository
import java.util.UUID
import kotlin.test.*

/** Explicit task only. Migrates the configured database, creates random fixtures, removes only those fixtures. */
class MySqlAuthIntegrationTest {
    @Test fun sharedHttpClientThroughKtorAndJdbcToRealMySql() = testApplication {
        val config = ServerConfig.fromEnvironment()
        val database = requireNotNull(config.database) { "MySQL integration requires external DB_URL, DB_USER and DB_PASSWORD" }
        val source = DatabaseFactory.connectAndMigrate(database)
        val ids = Role.entries.associateWith { UUID.randomUUID().toString() }
        val password = UUID.randomUUID().toString()
        val hash = PasswordHasher().hash(password)
        try {
            source.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery("SELECT version FROM flyway_schema_history WHERE success = TRUE ORDER BY installed_rank").use { rows ->
                        val versions = buildList { while (rows.next()) add(rows.getString(1)) }
                        assertTrue(versions.containsAll(listOf("1", "2")), "V1 and V2 must be applied successfully")
                    }
                }
                ids.forEach { (role, id) ->
                    connection.prepareStatement("INSERT INTO app_user(id,username,password_hash,role_code,active,provider_id) VALUES(?,?,?,?,TRUE,?)").use {
                        it.setString(1, id); it.setString(2, "rf34-test-$id"); it.setString(3, hash)
                        it.setString(4, role.name); it.setString(5, if (role == Role.PROVEEDOR) id else null)
                        it.executeUpdate()
                    }
                }
                connection.commit()
            }
            // Once any users exist, changing bootstrap configuration cannot provision another administrator.
            val rejectedBootstrapName = "rf34-test-bootstrap-${UUID.randomUUID()}"
            JdbcAuthPersistence(source).bootstrap(rejectedBootstrapName, password)
            source.connection.use { connection ->
                connection.prepareStatement("SELECT COUNT(*) FROM app_user WHERE username = ?").use {
                    it.setString(1, rejectedBootstrapName)
                    it.executeQuery().use { rows -> rows.next(); assertEquals(0, rows.getInt(1)) }
                }
            }
            application { configureHttp(); configureRouting(config, source, AuthService(JdbcAuthPersistence(source))) }
            val http = createClient { install(ContentNegotiation) { json() } }
            val repository = HttpAuthRepository(http)
            assertEquals(HttpStatusCode.OK, http.get("/health").status)
            val sessions = ids.mapValues { (role, id) ->
                val result = repository.login(LoginCommand("rf34-test-$id", password))
                val session = assertIs<AppResult.Success<SessionSnapshot>>(result).value
                assertEquals(role, session.user.role)
                assertIs<AppResult.Success<*>>(repository.validate(session.token.accessToken))
                session
            }
            val provider = sessions.getValue(Role.PROVEEDOR)
            assertEquals(HttpStatusCode.Forbidden, http.get("/api/v1/auth/permissions/MANAGE_USERS") { bearerAuth(provider.token.accessToken) }.status)
            assertEquals(HttpStatusCode.OK, http.get("/api/v1/auth/permissions/MANAGE_USERS") {
                bearerAuth(sessions.getValue(Role.ADMINISTRADOR_GENERAL).token.accessToken)
            }.status)
            assertEquals(HttpStatusCode.OK, http.get("/api/v1/auth/permissions/PROVIDER_HOME?providerId=${provider.user.userId}") { bearerAuth(provider.token.accessToken) }.status)
            assertEquals(HttpStatusCode.Forbidden, http.get("/api/v1/auth/permissions/PROVIDER_HOME?providerId=another") { bearerAuth(provider.token.accessToken) }.status)
            val store = MemorySessionStore().apply { write(provider) }
            assertIs<StartupDestination.Authorized>(RestoreSessionUseCase(store, repository)())
            val rotated = assertIs<AppResult.Success<SessionSnapshot>>(repository.refresh(provider.token.refreshToken)).value
            assertNotEquals(provider.token.refreshToken, rotated.token.refreshToken)
            assertIs<AppResult.Failure>(repository.refresh(provider.token.refreshToken))
            assertIs<AppResult.Failure>(repository.validate(rotated.token.accessToken))
            val collector = sessions.getValue(Role.ACOPIADOR)
            assertIs<AppResult.Success<*>>(repository.logout(collector.token.refreshToken))
            assertIs<AppResult.Failure>(repository.validate(collector.token.accessToken))
            assertIs<AppResult.Failure>(repository.refresh(collector.token.refreshToken))
            source.connection.use { connection ->
                connection.prepareStatement("UPDATE app_user SET active=FALSE WHERE id=?").use {
                    it.setString(1, ids.getValue(Role.PERSONAL_PLANTA)); it.executeUpdate()
                }
                connection.commit()
            }
            assertEquals(AppResult.Failure(AppError.AccountBlocked), repository.validate(sessions.getValue(Role.PERSONAL_PLANTA).token.accessToken))
        } finally {
            source.connection.use { connection ->
                ids.values.forEach { id ->
                    connection.prepareStatement("DELETE FROM auth_session WHERE user_id=?").use { it.setString(1, id); it.executeUpdate() }
                    connection.prepareStatement("DELETE FROM app_user WHERE id=?").use { it.setString(1, id); it.executeUpdate() }
                }
                connection.commit()
            }
            source.close()
        }
    }
}
