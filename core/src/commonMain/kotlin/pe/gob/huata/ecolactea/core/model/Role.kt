package pe.gob.huata.ecolactea.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    ADMINISTRADOR_GENERAL,
    PERSONAL_PLANTA,
    ACOPIADOR,
    PROVEEDOR,
}

@Serializable
data class AuthenticatedUser(
    val userId: String,
    val username: String,
    val role: Role,
    val providerId: String? = null,
)
