package com.example.ecolacteoshuata.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.example.ecolacteoshuata.core.util.BCrypt
import com.example.ecolacteoshuata.core.util.IdGenerator
import com.example.ecolacteoshuata.core.util.PasswordHasher
import com.example.ecolacteoshuata.core.util.nowLocalDateTime
import com.example.ecolacteoshuata.data.mapper.toDomain
import com.example.ecolacteoshuata.db.AppDatabase
import com.example.ecolacteoshuata.domain.model.Rol
import com.example.ecolacteoshuata.domain.model.Usuario
import com.example.ecolacteoshuata.domain.repository.UsuarioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class UsuarioRepositoryImpl(private val db: AppDatabase) : UsuarioRepository {
    private val queries = db.usuarioQueries

    override fun observeAll(): Flow<List<Usuario>> =
        queries.selectAll().asFlow().mapToList(Dispatchers.Default).map { list -> list.map { it.toDomain() } }

    override suspend fun getAll(): List<Usuario> = queries.selectAll().executeAsList().map { it.toDomain() }

    override suspend fun getById(id: String): Usuario? = queries.selectById(id).executeAsOneOrNull()?.toDomain()

    override suspend fun getByUsuario(usuario: String): Usuario? =
        queries.selectByUsuario(usuario).executeAsOneOrNull()?.toDomain()

    // Como `User::conAlgunRol` de Laravel: cuenta el rol principal y los adicionales.
    override suspend fun getByRol(rol: Rol): List<Usuario> =
        queries.selectAll().executeAsList().map { it.toDomain() }.filter { it.tieneRol(rol) }

    /**
     * bcrypt como Laravel: así el usuario creado en el teléfono se sube al servidor y entra con
     * la misma contraseña en la web. Tarda ~1 s en un teléfono: fuera del hilo de la interfaz.
     */
    private suspend fun cifrar(passwordPlano: String): String =
        withContext(Dispatchers.Default) { BCrypt.generar(passwordPlano) }

    override suspend fun crear(usuario: Usuario, passwordPlano: String) {
        val id = usuario.id.ifBlank { IdGenerator.newId() }
        val hash = cifrar(passwordPlano)
        queries.insert(
            id = id,
            nombres = usuario.nombres,
            apellidos = usuario.apellidos,
            documento = usuario.documento,
            celular = usuario.celular,
            rol = usuario.rol.name,
            usuario = usuario.usuario,
            password_hash = hash,
            activo = if (usuario.activo) 1L else 0L,
            creado_en = nowLocalDateTime().toString(),
        )
        queries.actualizarRoles(usuario.rol.name, Rol.codificarLista(usuario.rolesAdicionales), id)
    }

    override suspend fun actualizar(usuario: Usuario, passwordPlano: String?) {
        val hash = passwordPlano?.let { cifrar(it) } ?: usuario.passwordHash
        queries.update(
            nombres = usuario.nombres,
            apellidos = usuario.apellidos,
            documento = usuario.documento,
            celular = usuario.celular,
            rol = usuario.rol.name,
            usuario = usuario.usuario,
            password_hash = hash,
            activo = if (usuario.activo) 1L else 0L,
            id = usuario.id,
        )
        queries.actualizarRoles(usuario.rol.name, Rol.codificarLista(usuario.rolesAdicionales), usuario.id)
    }

    override suspend fun eliminar(id: String) {
        queries.deleteById(id)
    }

    override fun observeById(id: String): Flow<Usuario?> =
        queries.selectById(id).asFlow().mapToOneOrNull(Dispatchers.Default).map { it?.toDomain() }

    override suspend fun asignarTomaAsistencia(id: String, tomaAsistencia: Boolean) {
        val valor = if (tomaAsistencia) 1L else 0L
        queries.actualizarTomaAsistencia(valor, id, valor)
    }

    override suspend fun olvidarCambiosPendientes(id: String) {
        db.cambioPendienteQueries.olvidar("usuario", id)
    }

    override suspend fun countActivos(): Long = queries.countActivos().executeAsOne()

    override suspend fun countAll(): Long = queries.countAll().executeAsOne()

    override suspend fun verificarCredenciales(usuario: String, passwordPlano: String): Usuario? {
        val encontrado = getByUsuario(usuario) ?: return null
        if (!encontrado.activo) return null
        // El hash bcrypt de Laravel tarda ~1 s en un teléfono: fuera del hilo de la interfaz.
        val valida = withContext(Dispatchers.Default) { PasswordHasher.verify(passwordPlano, encontrado.passwordHash) }
        if (!valida) return null
        // Usuario creado en el teléfono antes de usar bcrypt: su hash viejo no sirve en Laravel.
        // Se aprovecha que se conoce la contraseña para pasarlo a bcrypt y poder subirlo.
        if (!BCrypt.esHash(encontrado.passwordHash)) {
            val hash = cifrar(passwordPlano)
            queries.actualizarPassword(hash, encontrado.id)
            return encontrado.copy(passwordHash = hash)
        }
        return encontrado
    }
}
