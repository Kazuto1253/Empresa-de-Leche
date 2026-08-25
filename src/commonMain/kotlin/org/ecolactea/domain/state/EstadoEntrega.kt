package org.ecolactea.domain.state

sealed interface EstadoEntrega {
    data object Vigente : EstadoEntrega
    data object Anulada : EstadoEntrega
}
