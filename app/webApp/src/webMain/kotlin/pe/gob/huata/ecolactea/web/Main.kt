package pe.gob.huata.ecolactea.web

import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import pe.gob.huata.ecolactea.App
import pe.gob.huata.ecolactea.core.application.auth.MemorySessionStore
import pe.gob.huata.ecolactea.shared.config.AppEnvironment

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
fun main() {
    // No tokens in localStorage, sessionStorage, IndexedDB or SQLite.
    val sessionStore = MemorySessionStore()
    ComposeViewport(document.body!!) {
        App(sessionStore, AppEnvironment(window.location.origin))
    }
}
