package org.ecolactea.domain.model

import org.ecolactea.domain.state.EstadoCalidad

/**
 * Resultado de densidad asociado a una entrega.
 *
 * RN-07/RN-10: la planta registra densidad y el proveedor recibe el resultado.
 * Los rangos de aceptación todavía no están validados, por lo que no se aplican.
 */
data class AnalisisCalidad(
    val id: String,
    val entregaId: String,
    val densidad: Double,
    val unidad: String,
    val fecha: String,
    val estado: EstadoCalidad = EstadoCalidad.Registrado
) {
    init {
        require(id.isNotBlank()) { "El ID del análisis es obligatorio." }
        require(entregaId.isNotBlank()) { "La entrega asociada es obligatoria." }
        require(densidad > 0.0) { "La densidad debe ser mayor que cero." }
        require(unidad.isNotBlank()) { "La unidad es obligatoria." }
        require(fecha.isNotBlank()) { "La fecha es obligatoria." }
    }
}
