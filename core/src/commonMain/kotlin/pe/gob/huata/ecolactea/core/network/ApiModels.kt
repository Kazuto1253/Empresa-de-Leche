package pe.gob.huata.ecolactea.core.network

import kotlinx.serialization.Serializable
import pe.gob.huata.ecolactea.core.model.Role

@Serializable
data class ApiEnvelope<T>(
    val data: T? = null,
    val error: ApiErrorDto? = null,
)

@Serializable
data class ApiErrorDto(
    val code: String,
    val message: String,
    val correlationId: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
)

@Serializable
data class RoleCatalogDto(
    val roles: List<Role>,
)
