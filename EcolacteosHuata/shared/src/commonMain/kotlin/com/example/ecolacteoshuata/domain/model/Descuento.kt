package com.example.ecolacteoshuata.domain.model

/** Descuento del punto de venta; [activo] = "Disponible para el vendedor". */
data class Descuento(
    val id: String,
    val nombre: String,
    val porcentaje: Double,
    val activo: Boolean,
)
