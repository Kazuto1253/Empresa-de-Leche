package com.example.ecolacteoshuata.domain.model

data class UsuarioRemoto(
    val id: String,
    val usuario: String,
    val nombres: String,
    val apellidos: String,
    val documento: String,
    val celular: String,
    val rol: Rol,
    val activo: Boolean,
    /** Token para pedir `/api/mobile/sync`. */
    val token: String? = null,
)
