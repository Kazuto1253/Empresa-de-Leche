package pe.gob.huata.ecolactea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import pe.gob.huata.ecolactea.shared.auth.AndroidSessionStore
import pe.gob.huata.ecolactea.shared.config.AppEnvironment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pe.gob.huata.ecolactea.core.application.sync.ConnectivitySyncCoordinator
import pe.gob.huata.ecolactea.core.application.sync.OutboxSyncManager
import pe.gob.huata.ecolactea.shared.network.OperationsRepository
import pe.gob.huata.ecolactea.shared.network.createApiClient
import pe.gob.huata.ecolactea.shared.sync.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val sessionStore = AndroidSessionStore(applicationContext)
        val offlineStore = AndroidOfflineCollectionStore(applicationContext)
        val environment = AppEnvironment(BuildConfig.API_BASE_URL)
        val syncClient = createApiClient(environment)
        val syncManager = OutboxSyncManager(offlineStore, CollectionSyncGateway(OperationsRepository(syncClient)) { sessionStore.read()?.token?.accessToken })
        val connectivity = AndroidConnectivityEvents(applicationContext)
        val coordinator = ConnectivitySyncCoordinator(connectivity) { lifecycleScope.launch { syncManager.synchronizePending() } }
        coordinator.start()
        setContent {
            App(sessionStore, environment, offlineStore) { syncManager.synchronizePending() }
        }
        lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onDestroy(owner: androidx.lifecycle.LifecycleOwner) { coordinator.stop(); syncClient.close() }
        })
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
