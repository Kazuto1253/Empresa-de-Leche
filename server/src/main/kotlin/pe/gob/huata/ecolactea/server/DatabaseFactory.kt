package pe.gob.huata.ecolactea.server

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database

object DatabaseFactory {
    fun connectAndMigrate(config: DatabaseConfig): HikariDataSource {
        require(config.user.isNotBlank()) { "DB_USER is required when DB_URL is configured" }
        require(config.password.isNotBlank()) { "DB_PASSWORD is required when DB_URL is configured" }

        val dataSource = HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = config.url
                username = config.user
                password = config.password
                driverClassName = "com.mysql.cj.jdbc.Driver"
                maximumPoolSize = config.maxPoolSize
                minimumIdle = 1
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_READ_COMMITTED"
            },
        )

        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
            .migrate()

        Database.connect(dataSource)
        return dataSource
    }
}
