package pe.gob.huata.ecolactea.server

data class ServerConfig(
    val host: String,
    val port: Int,
    val database: DatabaseConfig?,
) {
    companion object {
        fun fromEnvironment(env: Map<String, String> = System.getenv()): ServerConfig {
            val databaseUrl = env["DB_URL"]?.takeIf(String::isNotBlank)
            return ServerConfig(
                host = env["SERVER_HOST"]?.takeIf(String::isNotBlank) ?: "0.0.0.0",
                port = env["SERVER_PORT"]?.toIntOrNull() ?: 8080,
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

data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int,
)
