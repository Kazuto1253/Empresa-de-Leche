package pe.gob.huata.ecolactea.shared.sync

import android.content.Context
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import pe.gob.huata.ecolactea.core.application.sync.OfflineCollectionStore
import pe.gob.huata.ecolactea.core.application.sync.PendingOperation
import pe.gob.huata.ecolactea.shared.network.ecolacteaJson

class AndroidOfflineCollectionStore(context: Context) : OfflineCollectionStore {
    private val preferences = context.getSharedPreferences("collection_outbox", Context.MODE_PRIVATE)
    private val serializer = ListSerializer(PendingOperation.serializer())
    private val lock = Any()

    override suspend fun enqueue(operation: PendingOperation) = synchronized(lock) {
        val current = readAll().filterNot { it.idempotencyKey == operation.idempotencyKey } + operation
        writeAll(current)
    }

    override suspend fun pending(limit: Int): List<PendingOperation> = synchronized(lock) {
        readAll().filter { it.state == pe.gob.huata.ecolactea.core.application.sync.SyncState.PENDING || it.state == pe.gob.huata.ecolactea.core.application.sync.SyncState.FAILED_RETRYABLE || it.state == pe.gob.huata.ecolactea.core.application.sync.SyncState.FAILED }.take(limit)
    }

    override suspend fun update(operation: PendingOperation) = synchronized(lock) {
        writeAll(readAll().map { if (it.operationId == operation.operationId) operation else it })
    }

    private fun readAll(): List<PendingOperation> = runCatching { ecolacteaJson.decodeFromString(serializer, preferences.getString("operations", "[]") ?: "[]") }.getOrDefault(emptyList())
    private fun writeAll(value: List<PendingOperation>) { preferences.edit().putString("operations", ecolacteaJson.encodeToString(serializer, value)).apply() }
}
