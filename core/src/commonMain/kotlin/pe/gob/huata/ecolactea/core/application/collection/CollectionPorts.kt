package pe.gob.huata.ecolactea.core.application.collection

import pe.gob.huata.ecolactea.core.network.CollectionRecord
import pe.gob.huata.ecolactea.core.network.ReconciliationRecord

typealias ClientOperationId = String

interface ProviderRoutePlanQuery {
    suspend fun providerIsAssigned(providerId: String, routeId: String, at: String): Boolean
}

interface CollectionEntryPort {
    suspend fun entriesForProvider(providerId: String): List<CollectionRecord>
}

interface CollectionQuery {
    suspend fun collectionsForProvider(providerId: String): List<CollectionRecord>
}

interface CollectionSourceReference {
    val collectionId: String
}

interface CollectionLedgerQuery {
    suspend fun fieldTotals(workdayId: String): ReconciliationRecord?
}

interface ReceptionQuery {
    suspend fun reconciliation(workdayId: String): ReconciliationRecord?
}

interface MeasurementSourcePort {
    suspend fun readLiters(): String
}

interface FlowMeterPort : MeasurementSourcePort
