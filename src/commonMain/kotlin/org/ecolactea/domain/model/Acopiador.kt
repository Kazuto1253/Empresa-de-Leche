package org.ecolactea.domain.model

import org.ecolactea.domain.state.EstadoAcopiador

/**
 * Acopiador que registra diariamente la leche recepcionada por sectores.
 *
 * RN-02 y RN-03.
 */
data class Acopiador(
    val id: String,
    val nombre: String,
    val telefono: String,
    val estado: EstadoAcopiador = EstadoAcopiador.Activo
) {
    init {
        require(id.isNotBlank()) { "El ID del acopiador es obligatorio." }
        require(nombre.isNotBlank()) { "El nombre del acopiador es obligatorio." }
        require(telefono.isNotBlank()) { "El teléfono del acopiador es obligatorio." }
    }

    fun puedeRegistrarAcopio(): Boolean = estado is EstadoAcopiador.Activo
}
