package com.example.ecolacteoshuata.data.mapper

import com.example.ecolacteoshuata.data.remote.dto.LoginResponseDto
import com.example.ecolacteoshuata.domain.model.Rol
import com.example.ecolacteoshuata.domain.model.UsuarioRemoto

/** DTO del login -> dominio; `null` si Laravel mandó un rol que la app no conoce. */
fun LoginResponseDto.toDomain(): UsuarioRemoto? {
    val rol = Rol.fromServerValue(rol) ?: return null
    return UsuarioRemoto(
        id = id,
        usuario = usuario,
        nombres = nombres,
        apellidos = apellidos,
        documento = documento,
        celular = celular,
        rol = rol,
        activo = activo,
        token = token,
    )
}
