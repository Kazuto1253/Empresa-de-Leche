package pe.gob.huata.ecolactea.core.network

import kotlinx.serialization.Serializable

@Serializable data class CollectionEntryCommand(val providerId: String, val routeId: String, val workdayId: String, val collectedAt: String, val liters: String, val origin: String = "FIELD", val clientOperationId: String)
@Serializable data class DirectDeliveryCommand(val providerId: String, val deliveredAt: String, val liters: String, val clientOperationId: String)
@Serializable data class PlantReceptionCommand(val workdayId: String? = null, val routeId: String? = null, val collectionUnitId: String? = null, val directDeliveryId: String? = null, val receivedAt: String, val litersReceived: String, val measurementSource: String, val clientOperationId: String)
@Serializable data class ReconciliationCommand(val workdayId: String? = null, val routeId: String? = null)
@Serializable data class DiscrepancyResolutionCommand(val reason: String, val observations: String? = null, val authorizedCorrection: String? = null, val status: String = "RESOLVED")
@Serializable data class CollectionRecord(val id: String, val providerId: String? = null, val routeId: String? = null, val workdayId: String? = null, val liters: String, val origin: String, val status: String, val occurredAt: String)
@Serializable data class ReconciliationRecord(val id: String, val workdayId: String?, val routeId: String?, val totalField: String, val totalDirect: String, val totalExpected: String, val totalReceived: String, val difference: String, val status: String)
