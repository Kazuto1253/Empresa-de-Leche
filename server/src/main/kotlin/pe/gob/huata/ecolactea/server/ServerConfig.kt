package pe.gob.huata.ecolactea.server

data class ServerConfig(
    val host: String,
    val port: Int,
    val database: DatabaseConfig?,
    val bootstrap: BootstrapConfig? = null,
    val webRoot: String? = null,
) {
    companion object {
        fun fromEnvironment(env: Map<String, String> = ExternalConfig.load()): ServerConfig {
            val databaseUrl = env["DB_URL"]?.takeIf(String::isNotBlank)
            return ServerConfig(
                host = env["SERVER_HOST"]?.takeIf(String::isNotBlank) ?: "0.0.0.0",
                port = env["SERVER_PORT"]?.toIntOrNull() ?: 8080,
                webRoot = env["WEB_ROOT"]?.takeIf(String::isNotBlank),
                bootstrap = if (env["AUTH_BOOTSTRAP_ENABLED"] == "true") BootstrapConfig(
                    requireNotNull(env["AUTH_BOOTSTRAP_USERNAME"]) { "Bootstrap username required" },
                    requireNotNull(env["AUTH_BOOTSTRAP_PASSWORD"]) { "Bootstrap password required" },
                ) else null,
                database = databaseUrl?.let {
                    DatabaseConfig(
                        url = it,
                        user = env["DB_USER"].orEmpty(),
                        password = env["DB_PASSWORD"].orEmpty(),
                        maxPoolSize = env["DB_POOL_SIZE"]?.toIntOrNull() ?: 10,
                    )
                },
            )
        }
    }
}

class BootstrapConfig(val username: String, val password: String)

data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int,
) {
    override fun toString() = "DatabaseConfig([REDACTED])"
}
