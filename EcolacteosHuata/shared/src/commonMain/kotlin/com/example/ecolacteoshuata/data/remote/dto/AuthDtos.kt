package com.example.ecolacteoshuata.data.remote.dto

import kotlinx.serialization.Serializable

// Contrato JSON de `POST /api/mobile/login` (MobileAuthController de Laravel).

@Serializable
data class LoginRequestDto(val usuario: String, val password: String)

@Serializable
data class LoginResponseDto(
    val id: String,
    val usuario: String,
    val nombres: String,
    val apellidos: String,
    val documento: String = "",
    val celular: String = "",
    val rol: String? = null,
    val activo: Boolean = true,
    /** Token del dispositivo: se guarda en TokenStorage (Keystore / Keychain), nunca en SQLite. */
    val token: String? = null,
)

@Serializable
data class ErrorResponseDto(val message: String? = null)
