package pe.gob.huata.ecolactea.server

import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import pe.gob.huata.ecolactea.server.auth.AuthService
import pe.gob.huata.ecolactea.server.auth.JdbcAuthPersistence

fun main() {
    val config = ServerConfig.fromEnvironment()
    embeddedServer(Netty, port = config.port, host = config.host) {
        module(config)
    }.start(wait = true)
}

fun Application.module(config: ServerConfig = ServerConfig.fromEnvironment()) {
    val dataSource: HikariDataSource? = config.database?.let(DatabaseFactory::connectAndMigrate)
    val auth = dataSource?.let { source ->
        val persistence = JdbcAuthPersistence(source)
        config.bootstrap?.let { persistence.bootstrap(it.username, it.password) }
        AuthService(persistence)
    }
    configureHttp()
    configureRouting(config, dataSource, auth)
    monitor.subscribe(io.ktor.server.application.ApplicationStopped) { dataSource?.close() }
}
