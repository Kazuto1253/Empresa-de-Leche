package org.ecolactea.domain.model

import org.ecolactea.domain.enums.TipoEvento

/**
 * Reunión o capacitación.
 *
 * RN-11 / RF-14.
 */
data class Evento(
    val id: String,
    val tema: String,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val tipo: TipoEvento
) {
    init {
        require(id.isNotBlank()) { "El ID del evento es obligatorio." }
        require(tema.isNotBlank()) { "El tema es obligatorio." }
        require(fecha.isNotBlank()) { "La fecha es obligatoria." }
        require(horaInicio.isValidHour()) { "La hora de inicio debe usar HH:mm." }
        require(horaFin.isValidHour()) { "La hora de fin debe usar HH:mm." }
        require(horaFin > horaInicio) {
            "La hora de fin debe ser posterior a la hora de inicio."
        }
    }

    private fun String.isValidHour(): Boolean =
        matches(Regex("""^([01]\d|2[0-3]):[0-5]\d$"""))
}
