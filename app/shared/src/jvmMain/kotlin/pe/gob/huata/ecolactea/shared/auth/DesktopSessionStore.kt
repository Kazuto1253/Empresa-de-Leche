package pe.gob.huata.ecolactea.shared.auth

import com.sun.jna.platform.win32.Crypt32Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import pe.gob.huata.ecolactea.core.application.auth.*
import pe.gob.huata.ecolactea.shared.network.ecolacteaJson
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

fun desktopSessionStore(): SessionStore = if (System.getProperty("os.name").startsWith("Windows")) {
    WindowsSessionStore(Path.of(System.getenv("LOCALAPPDATA") ?: error("LOCALAPPDATA unavailable"), "Ecolactea", "auth.session"))
} else MemorySessionStore()

/** DPAPI user scope; neither plaintext tokens nor encryption keys are stored in the file. */
class WindowsSessionStore(private val path: Path) : SessionStore {
    override suspend fun read(): SessionSnapshot? = withContext(Dispatchers.IO) {
        if (!Files.exists(path)) return@withContext null
        try {
            require(Files.size(path) in 1..65536)
            val plain = Crypt32Util.cryptUnprotectData(Files.readAllBytes(path))
            try { ecolacteaJson.decodeFromString<SessionSnapshot>(plain.decodeToString()) }
            finally { plain.fill(0) }
        } catch (_: Exception) { clear(); null }
    }
    override suspend fun write(session: SessionSnapshot) = withContext(Dispatchers.IO) {
        Files.createDirectories(path.parent)
        val plain = ecolacteaJson.encodeToString(session).encodeToByteArray()
        val encrypted = try { Crypt32Util.cryptProtectData(plain) } finally { plain.fill(0) }
        val temporary = Files.createTempFile(path.parent, "auth-", ".encrypted")
        try {
            Files.write(temporary, encrypted)
            Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            Unit
        } finally { Files.deleteIfExists(temporary) }
    }
    override suspend fun clear() = withContext(Dispatchers.IO) { Files.deleteIfExists(path); Unit }
}
