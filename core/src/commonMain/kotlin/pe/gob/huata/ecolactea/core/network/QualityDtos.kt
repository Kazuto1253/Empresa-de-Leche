package pe.gob.huata.ecolactea.core.network

import kotlinx.serialization.Serializable

@Serializable data class QualityAnalysisCommand(val providerId: String, val sourceType: String, val sourceId: String, val sampleReference: String? = null, val analyzedAt: String, val observations: String? = null)
@Serializable data class QualityParameterCommand(val key: String, val name: String, val unit: String? = null, val valueType: String = "DECIMAL")
@Serializable data class QualityResultCommand(val parameterKey: String, val decimalValue: String? = null, val textValue: String? = null, val unit: String? = null)
@Serializable data class QualityClassificationCommand(val observations: String? = null)
@Serializable data class QualityIncidentCommand(val providerId: String, val analysisId: String? = null, val classificationId: String? = null, val category: String, val description: String, val severity: String, val occurredAt: String)
@Serializable data class QualityMeasureCommand(val measureType: String, val description: String, val startsAt: String, val endsAt: String? = null, val responsibleUserId: String, val observations: String? = null)
@Serializable data class TechnicalFollowUpCommand(val providerId: String, val incidentId: String? = null, val followUpType: String, val scheduledAt: String? = null, val performedAt: String? = null, val responsibleUserId: String, val result: String? = null, val notes: String? = null, val status: String = "SCHEDULED")
@Serializable data class ProviderNotificationCommand(val providerId: String, val notificationType: String, val title: String, val body: String, val sourceType: String? = null, val sourceId: String? = null)
@Serializable data class CommunicationEventCommand(val eventType: String, val title: String, val description: String, val startsAt: String, val endsAt: String? = null, val location: String? = null, val audience: String)
@Serializable data class AttendanceCommand(val providerId: String, val status: String, val markingMethod: String = "MANUAL")
@Serializable data class QualityAnalysisView(val id: String, val providerId: String, val sourceType: String, val sourceId: String, val analyzedAt: String, val status: String, val observations: String? = null)
@Serializable data class QualityResultView(val id: String, val analysisId: String, val parameterKey: String, val decimalValue: String? = null, val textValue: String? = null, val unit: String? = null)
@Serializable data class QualityClassificationView(val id: String, val analysisId: String, val classification: String, val ruleVersion: String, val status: String, val classifiedAt: String)
@Serializable data class CommunicationEventView(val id: String, val eventType: String, val title: String, val description: String, val startsAt: String, val audience: String, val status: String)
@Serializable data class QualityListItem(val id: String, val subjectId: String, val name: String, val detail: String, val status: String)
