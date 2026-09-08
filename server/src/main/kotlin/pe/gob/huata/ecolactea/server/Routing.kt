package pe.gob.huata.ecolactea.server

import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import pe.gob.huata.ecolactea.server.auth.AuthService
import pe.gob.huata.ecolactea.server.auth.authRoutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.ktor.server.http.content.staticFiles
import java.io.File

@Serializable
data class HealthResponse(
    val status: String,
    val service: String,
)

@Serializable
data class DatabaseHealthResponse(
    val configured: Boolean,
    val reachable: Boolean,
)

fun Application.configureRouting(
    config: ServerConfig,
    dataSource: HikariDataSource?,
    auth: AuthService? = null,
) {
    routing {
        authRoutes(auth)
        config.webRoot?.let { root ->
            require(File(root, "index.html").isFile) { "WEB_ROOT must contain the generated index.html" }
            // Root-only navigation currently needs no SPA catch-all; unknown /api routes remain 404.
            staticFiles("/", File(root))
        }
        get("/health") {
            call.respond(HealthResponse(status = "ok", service = "ecolactea-server"))
        }

        get("/health/db") {
            call.respond(
                DatabaseHealthResponse(
                    configured = config.database != null,
                    reachable = withContext(Dispatchers.IO) {
                        try { dataSource?.connection?.use { it.isValid(2) } == true }
                        catch (_: Exception) { false }
                    },
                ),
            )
        }
    }
}
