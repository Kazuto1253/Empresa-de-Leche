package org.ecolactea.domain.state

sealed interface EstadoObservacion {
    data object Registrada : EstadoObservacion
    data object Confirmada : EstadoObservacion
    data object Descartada : EstadoObservacion
}
