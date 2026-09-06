package pe.gob.huata.ecolactea.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class RoleTest {
    @Test
    fun exposesExactlyTheFourApprovedRoles() {
        assertEquals(
            listOf("ADMINISTRADOR_GENERAL", "PERSONAL_PLANTA", "ACOPIADOR", "PROVEEDOR"),
            Role.entries.map(Role::name),
        )
    }
}
