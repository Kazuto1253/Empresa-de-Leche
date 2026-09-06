package pe.gob.huata.ecolactea.server

import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable

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
) {
    routing {
        get("/health") {
            call.respond(HealthResponse(status = "ok", service = "ecolactea-server"))
        }

        get("/health/db") {
            call.respond(
                DatabaseHealthResponse(
                    configured = config.database != null,
                    reachable = dataSource?.isRunning == true,
                ),
            )
        }
    }
}
