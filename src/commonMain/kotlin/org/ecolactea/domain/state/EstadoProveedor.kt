package org.ecolactea.domain.state

sealed interface EstadoProveedor {
    data object Activo : EstadoProveedor
    data object Inactivo : EstadoProveedor
}
