package pe.gob.huata.ecolactea.core.application.sync

import pe.gob.huata.ecolactea.core.application.AppError
import pe.gob.huata.ecolactea.core.application.AppResult

class OutboxSyncManager(
    private val store: OfflineCollectionStore,
    private val gateway: SyncGateway,
) : SyncManager {
    override suspend fun synchronizePending(limit: Int): AppResult<Int> {
        var synchronized = 0
        for (operation in store.pending(limit)) {
            when (val result = gateway.push(operation)) {
                is AppResult.Success -> {
                    val state = when (result.value) {
                        is SyncReceipt.Accepted, is SyncReceipt.Duplicate -> SyncState.SYNCED
                        is SyncReceipt.Conflicted -> SyncState.CONFLICT
                    }
                    store.update(operation.copy(state = state))
                    if (state == SyncState.SYNCED) synchronized++
                }
                is AppResult.Failure -> {
                    if (result.error is AppError.Remote && result.error.statusCode == 409) store.update(operation.copy(state = SyncState.CONFLICT))
                    else store.update(operation.copy(state = SyncState.FAILED_RETRYABLE, retryCount = operation.retryCount + 1))
                }
            }
        }
        return AppResult.Success(synchronized)
    }
}
