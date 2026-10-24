package com.example.ecolacteoshuata.domain.usecase

import com.example.ecolacteoshuata.core.util.AppResult
import com.example.ecolacteoshuata.core.util.IdGenerator
import com.example.ecolacteoshuata.core.util.Validators
import com.example.ecolacteoshuata.core.util.nowLocalDateTime
import com.example.ecolacteoshuata.domain.model.Rol
import com.example.ecolacteoshuata.domain.model.Usuario
import com.example.ecolacteoshuata.domain.repository.AcopiadorRepository
import com.example.ecolacteoshuata.domain.repository.ProveedorRepository
import com.example.ecolacteoshuata.domain.repository.UsuarioRepository
import com.example.ecolacteoshuata.domain.model.Acopiador
import com.example.ecolacteoshuata.domain.model.Proveedor
import kotlinx.coroutines.flow.Flow

class ObserveUsuariosUseCase(private val repository: UsuarioRepository) {
    operator fun invoke(): Flow<List<Usuario>> = repository.observeAll()
}

class GetUsuariosUseCase(private val repository: UsuarioRepository) {
    suspend operator fun invoke(): List<Usuario> = repository.getAll()
}

/** Datos de la finca que Laravel pide cuando el usuario tiene el rol Proveedor. */
data class DatosProveedorInput(
    val acopiadorId: String? = null,
    val fincaUbicacion: String = "",
    val fincaReferencia: String = "",
    /** Punto marcado en el mapa (`latitud`/`longitud` de Laravel); opcional. */
    val latitud: Double? = null,
    val longitud: Double? = null,
)

/** Formulario "Nuevo usuario" / "Editar usuario" (`id == null` crea uno nuevo). */
data class GuardarUsuarioInput(
    val id: String?,
    val nombres: String,
    val apellidos: String,
    val documento: String,
    val celular: String,
    val rol: Rol,
    val usuario: String,
    val password: String,
    val activo: Boolean,
    val proveedor: DatosProveedorInput = DatosProveedorInput(),
)

/**
 * Crea las filas de acopiador/proveedor que necesita un usuario según sus roles, igual que
 * `Acopiador::firstOrCreate` y `Proveedor::guardarConUbicacion` en Laravel.
 */
class PerfilesPorRol(
    private val proveedorRepository: ProveedorRepository,
    private val acopiadorRepository: AcopiadorRepository,
) {
    /** Mismas reglas que Laravel cuando uno de los roles es Proveedor. */
    fun validarProveedor(roles: Collection<Rol>, datos: DatosProveedorInput): String? {
        if (Rol.PROVEEDOR !in roles) return null
        if (datos.acopiadorId == null) return "Selecciona el acopiador del proveedor."
        if (!Validators.noVacio(datos.fincaUbicacion)) return "Ingresa la ubicación de la finca."
        return null
    }

    /**
     * [datos] `null` = solo asegurar que el perfil exista, sin tocar la finca ni el acopiador
     * de un proveedor que ya los tiene (lo usa "Asignar roles", que solo asigna roles).
     */
    suspend fun asegurar(usuario: Usuario, datos: DatosProveedorInput?) {
        if (usuario.tieneRol(Rol.ACOPIADOR) && acopiadorRepository.getByUsuarioId(usuario.id) == null) {
            acopiadorRepository.crear(
                Acopiador(id = IdGenerator.newId(), usuarioId = usuario.id, nombreCompleto = usuario.nombreCompleto, rutaNombre = "")
            )
        }
        if (usuario.tieneRol(Rol.PROVEEDOR)) {
            val existente = proveedorRepository.getByUsuarioId(usuario.id)
            if (existente == null) {
                proveedorRepository.crear(
                    Proveedor(
                        id = IdGenerator.newId(),
                        usuarioId = usuario.id,
                        nombreCompleto = usuario.nombreCompleto,
                        fincaUbicacion = datos?.fincaUbicacion.orEmpty().trim(),
                        fincaReferencia = datos?.fincaReferencia.orEmpty().trim(),
                        latitud = datos?.latitud,
                        longitud = datos?.longitud,
                        acopiadorId = datos?.acopiadorId,
                        bloqueado = false,
                        alteraciones = 0,
                    )
                )
            } else if (datos != null) {
                proveedorRepository.actualizarFinca(
                    existente.id, datos.fincaUbicacion.trim(), datos.fincaReferencia.trim(), datos.latitud, datos.longitud,
                )
                proveedorRepository.asignarAcopiador(existente.id, datos.acopiadorId)
            }
        }
    }
}

/** Guarda un usuario con las validaciones de `admin/usuarios` de Laravel. */
class GuardarUsuarioUseCase(
    private val usuarioRepository: UsuarioRepository,
    private val perfiles: PerfilesPorRol,
) {
    suspend operator fun invoke(input: GuardarUsuarioInput): AppResult<Usuario> {
        val documento = input.documento.trim()
        val nombreUsuario = input.usuario.trim()
        if (!Validators.noVacio(input.nombres) || !Validators.noVacio(input.apellidos)) {
            return AppResult.Error("Ingresa nombres y apellidos.")
        }
        if (!Validators.noVacio(documento) || documento.length > 15) {
            return AppResult.Error("Ingresa el DNI/RUC (máximo 15 caracteres).")
        }
        if (!Validators.noVacio(nombreUsuario)) return AppResult.Error("Ingresa el usuario de acceso.")
        // Al crear es obligatoria; al editar, vacía significa "no cambiarla".
        if ((input.id == null || input.password.isNotEmpty()) && input.password.length < 4) {
            return AppResult.Error("La contraseña debe tener al menos 4 caracteres.")
        }
        val otros = usuarioRepository.getAll().filter { it.id != input.id }
        if (otros.any { it.documento.trim() == documento }) return AppResult.Error("Ya existe un usuario con ese DNI/RUC.")
        if (otros.any { it.usuario.equals(nombreUsuario, ignoreCase = true) }) return AppResult.Error("Ese nombre de usuario ya existe.")

        val actual = input.id?.let { usuarioRepository.getById(it) }
        // Si el rol principal cambia, el anterior deja de ser adicional-implícito; los demás se conservan.
        val adicionales = actual?.rolesAdicionales.orEmpty() - input.rol
        perfiles.validarProveedor(adicionales + input.rol, input.proveedor)?.let { return AppResult.Error(it) }

        val usuario = Usuario(
            id = input.id ?: IdGenerator.newId(),
            nombres = input.nombres.trim(),
            apellidos = input.apellidos.trim(),
            documento = documento,
            celular = input.celular.trim(),
            rol = input.rol,
            usuario = nombreUsuario,
            passwordHash = actual?.passwordHash.orEmpty(),
            activo = input.activo,
            creadoEn = actual?.creadoEn ?: nowLocalDateTime(),
            rolesAdicionales = adicionales,
        )
        if (actual == null) {
            usuarioRepository.crear(usuario, input.password)
        } else {
            usuarioRepository.actualizar(usuario, input.password.takeIf { it.isNotBlank() })
        }
        perfiles.asegurar(usuario, input.proveedor)
        return AppResult.Success(usuario)
    }
}

/** "Asignar rol a usuarios" de Laravel: uno o varios roles por usuario. */
class AsignarRolesUseCase(
    private val usuarioRepository: UsuarioRepository,
    private val perfiles: PerfilesPorRol,
) {
    /** Solo asigna roles: los datos de la finca y el acopiador se cargan desde "Usuarios". */
    suspend operator fun invoke(usuarioId: String, roles: Set<Rol>, tomaAsistencia: Boolean): AppResult<Unit> {
        if (roles.isEmpty()) return AppResult.Error("Selecciona al menos un rol.")
        val usuario = usuarioRepository.getById(usuarioId) ?: return AppResult.Error("El usuario ya no existe.")
        // El rol principal se conserva si sigue marcado; si no, pasa a serlo el primero en el orden de Laravel.
        val principal = if (usuario.rol in roles) usuario.rol else Rol.ordenLaravel.first { it in roles }
        val actualizado = usuario.copy(rol = principal, rolesAdicionales = roles - principal, tomaAsistencia = tomaAsistencia)
        usuarioRepository.actualizar(actualizado, null)
        usuarioRepository.asignarTomaAsistencia(usuarioId, tomaAsistencia)
        perfiles.asegurar(actualizado, datos = null)
        return AppResult.Success(Unit)
    }
}

class ActualizarUsuarioUseCase(private val repository: UsuarioRepository) {
    suspend operator fun invoke(usuario: Usuario, nuevaPassword: String?): AppResult<Unit> {
        if (!Validators.noVacio(usuario.nombres) || !Validators.noVacio(usuario.apellidos)) {
            return AppResult.Error("Ingrese nombres y apellidos.")
        }
        repository.actualizar(usuario, nuevaPassword?.takeIf { it.isNotBlank() })
        return AppResult.Success(Unit)
    }
}

class EliminarUsuarioUseCase(private val repository: UsuarioRepository) {
    suspend operator fun invoke(id: String) = repository.eliminar(id)
}
