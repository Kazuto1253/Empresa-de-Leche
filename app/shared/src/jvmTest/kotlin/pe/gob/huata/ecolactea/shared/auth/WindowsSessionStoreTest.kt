package pe.gob.huata.ecolactea.shared.auth

import kotlinx.coroutines.test.runTest
import pe.gob.huata.ecolactea.core.application.auth.*
import pe.gob.huata.ecolactea.core.model.*
import java.nio.file.Files
import java.util.UUID
import kotlin.test.*

class WindowsSessionStoreTest {
    @Test fun dpapiPersistsAcrossInstancesAndRejectsTampering() = runTest {
        if (!System.getProperty("os.name").startsWith("Windows")) return@runTest
        val directory = Files.createTempDirectory("ecolactea-dpapi-test")
        val path = directory.resolve("session.encrypted")
        val session = SessionSnapshot(AuthenticatedUser("test", "test", Role.ACOPIADOR),
            SessionToken(UUID.randomUUID().toString(), UUID.randomUUID().toString(), 900))
        try {
            val store = WindowsSessionStore(path)
            assertNull(store.read())
            store.write(session)
            val bytes = Files.readAllBytes(path)
            assertFalse(bytes.decodeToString().contains(session.token.accessToken))
            assertFalse(bytes.decodeToString().contains(session.token.refreshToken))
            assertEquals(session, WindowsSessionStore(path).read())
            bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte()
            Files.write(path, bytes)
            assertNull(store.read()); assertFalse(Files.exists(path))
            store.write(session); store.clear(); assertNull(store.read())
        } finally { Files.deleteIfExists(path); Files.deleteIfExists(directory) }
    }
}
