package pe.gob.huata.ecolactea.core.network

import kotlinx.serialization.Serializable

@Serializable
data class RefreshCommand(val refreshToken: String) {
    override fun toString() = "RefreshCommand([REDACTED])"
}
