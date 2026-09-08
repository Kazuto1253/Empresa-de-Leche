package pe.gob.huata.ecolactea.core.application.sync

interface OfflineCollectionStore {
    suspend fun enqueue(operation: PendingOperation)
    suspend fun pending(limit: Int = 50): List<PendingOperation>
    suspend fun update(operation: PendingOperation)
}
