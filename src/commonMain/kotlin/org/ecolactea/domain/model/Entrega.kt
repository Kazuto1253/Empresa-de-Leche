package org.ecolactea.domain.model

import org.ecolactea.domain.enums.ModalidadEntrega
import org.ecolactea.domain.state.EstadoEntrega
import org.ecolactea.domain.state.EstadoSincronizacion

/**
 * Entidad principal del dominio.
 *
 * RN-01: existen entrega DIRECTA y ACOPIADOR.
 * RN-05: toda entrega debe tener proveedor y litros.
 * RF-05/RF-06: las modalidades tienen datos mínimos diferentes.
 */
data class Entrega(
    val id: String,
    val proveedorId: String,
    val fecha: String,
    val litros: Double,
    val modalidad: ModalidadEntrega,
    val acopiadorId: String? = null,
    val sector: String? = null,
    val estado: EstadoEntrega = EstadoEntrega.Vigente,
    val sincronizacion: EstadoSincronizacion = EstadoSincronizacion.Sincronizado
) {
    init {
        require(id.isNotBlank()) { "El ID de la entrega es obligatorio." }
        require(proveedorId.isNotBlank()) { "El proveedor es obligatorio." }
        require(fecha.isNotBlank()) { "La fecha es obligatoria." }
        require(litros > 0.0) { "La cantidad de litros debe ser mayor que cero." }

        when (modalidad) {
            ModalidadEntrega.DIRECTA -> {
                require(acopiadorId == null) {
                    "Una entrega directa no debe tener acopiador."
                }
                require(sector == null) {
                    "Una entrega directa no debe tener sector de acopio."
                }
            }

            ModalidadEntrega.ACOPIADOR -> {
                require(!acopiadorId.isNullOrBlank()) {
                    "Una entrega mediante acopiador debe tener acopiador."
                }
                require(!sector.isNullOrBlank()) {
                    "Una entrega mediante acopiador debe indicar sector."
                }
            }
        }
    }
}
