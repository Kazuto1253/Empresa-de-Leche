package org.ecolactea.domain.model

import org.ecolactea.domain.state.EstadoProveedor

/**
 * Proveedor de leche de la planta.
 *
 * RN-05: las entregas quedan asociadas al proveedor.
 * RF-02: un proveedor inactivo conserva su historial, pero no admite nuevas entregas.
 */
data class Proveedor(
    val id: String,
    val nombre: String,
    val telefono: String,
    val estado: EstadoProveedor = EstadoProveedor.Activo
) {
    init {
        require(id.isNotBlank()) { "El ID del proveedor es obligatorio." }
        require(nombre.isNotBlank()) { "El nombre del proveedor es obligatorio." }
        require(telefono.isNotBlank()) { "El teléfono del proveedor es obligatorio." }
    }

    fun puedeRegistrarEntrega(): Boolean = estado is EstadoProveedor.Activo
}
