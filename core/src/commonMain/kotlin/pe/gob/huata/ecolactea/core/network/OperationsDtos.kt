package pe.gob.huata.ecolactea.core.network

import kotlinx.serialization.Serializable

@Serializable data class ProviderListItem(val id: String, val code: String, val dni: String, val firstName: String, val lastName: String, val phone: String, val farmName: String? = null, val status: String)
@Serializable data class ProviderCreateCommand(val dni: String, val firstName: String, val lastName: String, val phone: String, val farmName: String? = null)
@Serializable data class OperationalItem(val id: String, val code: String, val name: String, val status: String)
@Serializable data class UserItem(val id: String, val username: String, val role: String, val active: Boolean, val providerId: String? = null)
@Serializable data class UnitCommand(val code: String, val plateOrIdentifier: String? = null, val description: String)
@Serializable data class WorkdayCommand(val code: String, val routeId: String, val unitId: String, val workDate: String, val plannedStartTime: String, val plannedEndTime: String? = null)
@Serializable data class ProductCommand(val code: String, val name: String, val category: String, val unit: String)
@Serializable data class CustomerCommand(val code: String, val name: String, val document: String? = null, val customerType: String, val phone: String? = null)
@Serializable data class PriceCommand(val productId: String, val customerType: String, val amount: String, val currency: String, val effectiveFrom: String, val effectiveTo: String? = null, val reason: String)
@Serializable data class ParameterCommand(val key: String, val value: String, val valueType: String, val unit: String? = null, val effectiveFrom: String, val reason: String? = null)
@Serializable data class ZoneCommand(val code: String, val name: String, val description: String? = null)
@Serializable data class RouteCommand(val code: String, val name: String, val zoneId: String, val description: String? = null, val validFrom: String)
