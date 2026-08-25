package org.ecolactea.domain.model

import org.ecolactea.domain.state.EstadoObservacion

/**
 * Observación de posible adulteración.
 *
 * RN-09: el proveedor relacionado debe ser notificado.
 * No se implementa una sanción automática porque el procedimiento está pendiente.
 */
data class ObservacionAdulteracion(
    val id: String,
    val entregaId: String,
    val motivo: String,
    val estado: EstadoObservacion = EstadoObservacion.Registrada
) {
    init {
        require(id.isNotBlank()) { "El ID de la observación es obligatorio." }
        require(entregaId.isNotBlank()) { "La entrega asociada es obligatoria." }
        require(motivo.isNotBlank()) { "El motivo de la observación es obligatorio." }
    }
}
