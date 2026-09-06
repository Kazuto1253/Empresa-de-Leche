package pe.gob.huata.ecolactea.shared.config

data class AppEnvironment(
    val apiBaseUrl: String,
) {
    companion object {
        val LocalDevelopment = AppEnvironment(apiBaseUrl = "http://localhost:8080")
    }
}
