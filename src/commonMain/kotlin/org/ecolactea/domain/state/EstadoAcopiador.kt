package org.ecolactea.domain.state

sealed interface EstadoAcopiador {
    data object Activo : EstadoAcopiador
    data object Inactivo : EstadoAcopiador
}
