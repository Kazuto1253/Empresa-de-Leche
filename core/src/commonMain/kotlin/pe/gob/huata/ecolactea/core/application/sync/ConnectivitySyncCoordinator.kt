package pe.gob.huata.ecolactea.core.application.sync

class ConnectivitySyncCoordinator(
    private val events: ConnectivityEvents,
    private val launchSync: () -> Unit,
) {
    fun start() = events.start(launchSync)
    fun synchronizeManually() = launchSync()
    fun stop() = events.stop()
}
