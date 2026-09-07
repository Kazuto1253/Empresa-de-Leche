package pe.gob.huata.ecolactea.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class Permission { VIEW_SESSION, ADMIN_HOME, PLANT_HOME, COLLECTION_HOME, PROVIDER_HOME, MANAGE_USERS }

object Authorization {
    fun permissions(role: Role): Set<Permission> = setOf(Permission.VIEW_SESSION) + when (role) {
        Role.ADMINISTRADOR_GENERAL -> setOf(Permission.ADMIN_HOME, Permission.MANAGE_USERS)
        Role.PERSONAL_PLANTA -> setOf(Permission.PLANT_HOME)
        Role.ACOPIADOR -> setOf(Permission.COLLECTION_HOME)
        Role.PROVEEDOR -> setOf(Permission.PROVIDER_HOME)
    }

    fun allows(user: AuthenticatedUser, permission: Permission): Boolean =
        permission in permissions(user.role)

    fun ownsProvider(user: AuthenticatedUser, providerId: String): Boolean =
        user.role == Role.PROVEEDOR && providerId.isNotBlank() && user.providerId == providerId
}
