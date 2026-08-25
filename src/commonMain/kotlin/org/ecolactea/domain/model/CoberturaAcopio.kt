package org.ecolactea.domain.model

/**
 * Relaciona un acopiador con un proveedor y un sector.
 *
 * La vigencia exacta y si un proveedor puede cambiar de acopiador
 * permanecen pendientes de validación (INC-02).
 */
data class CoberturaAcopio(
    val id: String,
    val acopiadorId: String,
    val proveedorId: String,
    val sector: String
) {
    init {
        require(id.isNotBlank()) { "El ID de cobertura es obligatorio." }
        require(acopiadorId.isNotBlank()) { "El acopiador es obligatorio." }
        require(proveedorId.isNotBlank()) { "El proveedor es obligatorio." }
        require(sector.isNotBlank()) { "El sector es obligatorio." }
    }
}
