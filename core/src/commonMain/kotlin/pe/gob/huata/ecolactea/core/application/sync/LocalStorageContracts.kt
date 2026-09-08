package pe.gob.huata.ecolactea.core.application.sync

/** Local data is isolated by account and installation; never includes authentication tokens. */
data class OfflineScope(val userId: String, val deviceId: String)

enum class LocalCollection { PROVIDERS, ROUTES, WORKDAYS, COLLECTIONS }

/** Versioned cache/record envelope. Feature modules own payload schemas and validation. */
data class LocalRecord(
    val collection: LocalCollection,
    val id: String,
    val payloadJson: String,
    val serverVersion: Long?,
    val syncState: SyncState,
)

data class SyncCursor(val stream: String, val cursor: String)

interface LocalTransaction : PendingOperationStore {
    suspend fun read(collection: LocalCollection, id: String): LocalRecord?
    suspend fun put(record: LocalRecord)
    suspend fun remove(collection: LocalCollection, id: String)
    suspend fun readCursor(stream: String): SyncCursor?
    suspend fun writeCursor(cursor: SyncCursor)
}

interface LocalDataStore {
    /** SQLite adapters must commit the record and outbox together, or roll back both on failure/cancellation.
     * All reads, writes and PendingOperationStore methods in this transaction use the same scope.
     * Do not perform remote calls inside the transaction. Tokens belong exclusively to SessionStore.
     */
    suspend fun <T> transaction(scope: OfflineScope, block: suspend LocalTransaction.() -> T): T
}
