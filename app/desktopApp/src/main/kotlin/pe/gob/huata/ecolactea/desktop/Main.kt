package pe.gob.huata.ecolactea.desktop

import pe.gob.huata.ecolactea.App
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.remember
import pe.gob.huata.ecolactea.shared.auth.desktopSessionStore
import pe.gob.huata.ecolactea.shared.config.AppEnvironment

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Ecoláctea Digital",
    ) {
        val store = remember { desktopSessionStore() }
        App(store, AppEnvironment(System.getenv("ECOLACTEA_API_URL") ?: "http://localhost:8080"))
    }
}
