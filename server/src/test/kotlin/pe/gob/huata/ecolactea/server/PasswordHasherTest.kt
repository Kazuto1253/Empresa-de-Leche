package pe.gob.huata.ecolactea.server

import pe.gob.huata.ecolactea.server.auth.PasswordHasher
import java.util.UUID
import kotlin.test.*

class PasswordHasherTest {
    @Test fun saltsAreUniqueAndPasswordsAreVerified() {
        val password = UUID.randomUUID().toString()
        val hasher = PasswordHasher()
        val first = hasher.hash(password)
        val second = hasher.hash(password)
        assertNotEquals(first, second)
        assertFalse(first.contains(password))
        assertTrue(hasher.verify(password, first))
        assertFalse(hasher.verify(UUID.randomUUID().toString(), first))
        assertFalse(hasher.verify(password, "malformed"))
    }
}
