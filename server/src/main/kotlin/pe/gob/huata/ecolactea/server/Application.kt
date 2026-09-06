package pe.gob.huata.ecolactea.server

import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val config = ServerConfig.fromEnvironment()
    embeddedServer(Netty, port = config.port, host = config.host) {
        module(config)
    }.start(wait = true)
}

fun Application.module(config: ServerConfig = ServerConfig.fromEnvironment()) {
    val dataSource: HikariDataSource? = config.database?.let(DatabaseFactory::connectAndMigrate)
    configureHttp()
    configureRouting(config, dataSource)
}
