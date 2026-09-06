package pe.gob.huata.ecolactea.core.application.sync

import kotlinx.serialization.Serializable
import pe.gob.huata.ecolactea.core.application.AppResult

@Serializable
enum class ConnectivityStatus {
    ONLINE,
    OFFLINE,
    UNKNOWN,
}

@Serializable
enum class SyncState {
    PENDING,
    SYNCING,
    SYNCHRONIZED,
    CONFLICT,
    FAILED,
}

@Serializable
data class PendingOperation(
    val operationId: String,
    val aggregateType: String,
    val aggregateId: String,
    val idempotencyKey: String,
    val baseVersion: Long?,
    val payloadJson: String,
    val createdAtEpochMillis: Long,
    val state: SyncState = SyncState.PENDING,
    val retryCount: Int = 0,
)

@Serializable
data class SyncConflict(
    val operationId: String,
    val aggregateId: String,
    val localPayloadJson: String,
    val serverPayloadJson: String,
    val localVersion: Long?,
    val serverVersion: Long,
)

sealed interface SyncReceipt {
    data class Accepted(val operationId: String, val serverVersion: Long) : SyncReceipt
    data class Duplicate(val operationId: String, val serverVersion: Long) : SyncReceipt
    data class Conflicted(val conflict: SyncConflict) : SyncReceipt
}

interface ConnectivityMonitor {
    suspend fun currentStatus(): ConnectivityStatus
}

interface PendingOperationStore {
    suspend fun enqueue(operation: PendingOperation)
    suspend fun pending(limit: Int): List<PendingOperation>
    suspend fun markSynchronizing(operationId: String)
    suspend fun markSynchronized(operationId: String, serverVersion: Long)
    suspend fun markConflict(conflict: SyncConflict)
    suspend fun markFailed(operationId: String)
}

interface SyncGateway {
    suspend fun push(operation: PendingOperation): AppResult<SyncReceipt>
}

interface SyncManager {
    suspend fun synchronizePending(limit: Int = 50): AppResult<Int>
}
