package pe.gob.huata.ecolactea.core.application.operations

import kotlinx.serialization.Serializable

@Serializable enum class SourceStatus { AVAILABLE, NOT_AVAILABLE_YET, EMPTY }
@Serializable data class ProviderRoutePlan(val providerId: String, val routeId: String, val validFrom: String, val validTo: String? = null)
@Serializable data class CollectionWorkdaySummary(val id: String, val routeId: String, val unitId: String, val workDate: String, val status: String)
@Serializable data class ProviderEligibility(val providerId: String, val eligible: Boolean, val reason: String? = null)
@Serializable data class QualityParameterDefinition(val key: String, val unit: String?, val status: String)
@Serializable data class CatalogProduct(val id: String, val code: String, val name: String, val unit: String, val status: String)
@Serializable data class CatalogCustomer(val id: String, val code: String, val name: String, val customerType: String, val status: String)

interface ProviderRoutePlanQuery { suspend fun currentPlan(providerId: String): ProviderRoutePlan? }
interface CollectionWorkdayQuery { suspend fun forDate(date: String): List<CollectionWorkdaySummary> }
interface ProviderEligibilityQuery { suspend fun eligibility(providerId: String): ProviderEligibility }
interface CollectionHistorySource { suspend fun status(providerId: String): SourceStatus }
interface QualityHistorySource { suspend fun status(providerId: String): SourceStatus }
interface SettlementHistorySource { suspend fun status(providerId: String): SourceStatus }
interface PaymentHistorySource { suspend fun status(providerId: String): SourceStatus }
interface IncidentHistorySource { suspend fun status(providerId: String): SourceStatus }
interface SystemParameterQuery { suspend fun current(key: String): String?; suspend fun history(key: String): List<String> }
interface QualityParameterCatalog { suspend fun list(): List<QualityParameterDefinition> }
interface ProductCatalogQuery { suspend fun products(): List<CatalogProduct> }
interface CustomerCatalogQuery { suspend fun customers(): List<CatalogCustomer> }
interface PriceResolver { suspend fun resolve(productId: String, customerType: String, date: String): String? }
