package org.ecolactea.domain.state

sealed interface EstadoSincronizacion {
    data object Sincronizado : EstadoSincronizacion
    data object Pendiente : EstadoSincronizacion
    data class Conflicto(val motivo: String) : EstadoSincronizacion
}
