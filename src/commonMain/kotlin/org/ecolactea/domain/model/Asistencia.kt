package org.ecolactea.domain.model

import org.ecolactea.domain.enums.EstadoAsistencia

/**
 * Asistencia de un proveedor convocado a un evento.
 *
 * El método exacto de marcado (lista, QR u otro) queda pendiente de validación.
 */
data class Asistencia(
    val id: String,
    val eventoId: String,
    val proveedorId: String,
    val estado: EstadoAsistencia
) {
    init {
        require(id.isNotBlank()) { "El ID de asistencia es obligatorio." }
        require(eventoId.isNotBlank()) { "El evento es obligatorio." }
        require(proveedorId.isNotBlank()) { "El proveedor es obligatorio." }
    }
}
