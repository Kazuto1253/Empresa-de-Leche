package pe.gob.huata.ecolactea.shared.sync

import kotlinx.serialization.encodeToString
import pe.gob.huata.ecolactea.core.application.*
import pe.gob.huata.ecolactea.core.application.sync.*
import pe.gob.huata.ecolactea.core.network.CollectionEntryCommand
import pe.gob.huata.ecolactea.core.network.CollectionRecord
import pe.gob.huata.ecolactea.shared.network.OperationsRepository
import pe.gob.huata.ecolactea.shared.network.ecolacteaJson

class CollectionSyncGateway(
    private val repository: OperationsRepository,
    private val tokenProvider: suspend () -> String?,
) : SyncGateway {
    override suspend fun push(operation: PendingOperation): AppResult<SyncReceipt> {
        val token = tokenProvider() ?: return AppResult.Failure(AppError.Unauthorized)
        val command = runCatching { ecolacteaJson.decodeFromString<CollectionEntryCommand>(operation.payloadJson) }
            .getOrElse { return AppResult.Failure(AppError.Validation(mapOf("payload" to "Operación local inválida"))) }
        return when (val result = repository.createCollection(token, command)) {
            is AppResult.Success -> AppResult.Success(SyncReceipt.Accepted(operation.operationId, 1))
            is AppResult.Failure -> result
        }
    }
}

class CollectionSubmissionService(
    private val repository: OperationsRepository,
    private val store: OfflineCollectionStore?,
) {
    suspend fun submit(token: String, command: CollectionEntryCommand): AppResult<CollectionRecord> {
        val result = repository.createCollection(token, command)
        if (result is AppResult.Failure && result.error is AppError.Offline && store != null) {
            store.enqueue(PendingOperation(command.clientOperationId,"MILK_COLLECTION",command.clientOperationId,command.clientOperationId,null,ecolacteaJson.encodeToString(command),kotlin.time.Clock.System.now().toEpochMilliseconds(),SyncState.PENDING))
        }
        return result
    }
}
