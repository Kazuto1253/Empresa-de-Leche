package com.example.ecolacteoshuata.presentation.admin.roles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecolacteoshuata.core.util.AppResult
import com.example.ecolacteoshuata.domain.model.Rol
import com.example.ecolacteoshuata.domain.model.Usuario
import com.example.ecolacteoshuata.domain.usecase.AsignarRolesUseCase
import com.example.ecolacteoshuata.domain.usecase.ObserveUsuariosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Formulario "Asignar roles" abierto para un usuario (el modal de Laravel). */
data class AsignarRolesForm(
    val usuario: Usuario,
    val roles: Set<Rol>,
    /** "Tomar asistencia": acceso al módulo de asistencia y justificaciones. */
    val tomaAsistencia: Boolean = false,
    val error: String? = null,
    val guardando: Boolean = false,
)

class AdminAsignarRolesViewModel(
    observeUsuariosUseCase: ObserveUsuariosUseCase,
    private val asignarRolesUseCase: AsignarRolesUseCase,
) : ViewModel() {
    val usuarios: StateFlow<List<Usuario>> = observeUsuariosUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _form = MutableStateFlow<AsignarRolesForm?>(null)
    val form: StateFlow<AsignarRolesForm?> = _form.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun abrir(usuario: Usuario) {
        _form.value = AsignarRolesForm(usuario = usuario, roles = usuario.rolesAsignados.toSet(), tomaAsistencia = usuario.tomaAsistencia)
    }

    fun alternarTomaAsistencia() = editar { copy(tomaAsistencia = !tomaAsistencia) }

    fun cerrar() { _form.value = null }

    /** Marca o desmarca un rol (volver a tocar uno marcado lo quita, como en Laravel). */
    fun alternarRol(rol: Rol) = editar { copy(roles = if (rol in roles) roles - rol else roles + rol) }

    fun guardar() {
        val f = _form.value ?: return
        if (f.guardando) return
        _form.value = f.copy(guardando = true, error = null)
        viewModelScope.launch {
            when (val r = asignarRolesUseCase(f.usuario.id, f.roles, f.tomaAsistencia)) {
                is AppResult.Success -> {
                    _form.value = null
                    _mensaje.value = "Roles actualizados correctamente."
                }
                is AppResult.Error -> _form.value = f.copy(guardando = false, error = r.message)
            }
        }
    }

    fun consumirMensaje() { _mensaje.value = null }

    private fun editar(cambio: AsignarRolesForm.() -> AsignarRolesForm) {
        _form.value = _form.value?.cambio()?.copy(error = null)
    }
}
