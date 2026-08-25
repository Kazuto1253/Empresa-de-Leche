package org.ecolactea.domain.state

sealed interface EstadoCalidad {
    data object Registrado : EstadoCalidad
    data object Observado : EstadoCalidad
}
