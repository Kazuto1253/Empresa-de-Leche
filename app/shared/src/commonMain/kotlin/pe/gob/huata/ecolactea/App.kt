package pe.gob.huata.ecolactea

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect
import pe.gob.huata.ecolactea.core.application.auth.MemorySessionStore
import pe.gob.huata.ecolactea.core.application.auth.SessionStore
import pe.gob.huata.ecolactea.shared.config.AppEnvironment
import pe.gob.huata.ecolactea.shared.network.createApiClient
import pe.gob.huata.ecolactea.shared.network.HttpAuthRepository
import pe.gob.huata.ecolactea.shared.presentation.AuthController
import pe.gob.huata.ecolactea.shared.presentation.EcolacteaApp
import pe.gob.huata.ecolactea.core.application.AppResult
import pe.gob.huata.ecolactea.core.application.sync.OfflineCollectionStore

@Composable
fun App(store: SessionStore = remember { MemorySessionStore() }, environment: AppEnvironment = AppEnvironment.LocalDevelopment, offlineStore: OfflineCollectionStore? = null, syncPending: (suspend () -> AppResult<Int>)? = null) {
    val client = remember(environment) { createApiClient(environment) }
    DisposableEffect(client) { onDispose { client.close() } }
    val controller = remember(store, client) { AuthController(HttpAuthRepository(client), store) }
    EcolacteaApp(controller, client, offlineStore, syncPending)
}
