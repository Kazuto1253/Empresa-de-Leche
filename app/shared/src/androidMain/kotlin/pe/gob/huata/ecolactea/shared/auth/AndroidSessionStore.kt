package pe.gob.huata.ecolactea.shared.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import pe.gob.huata.ecolactea.core.application.auth.SessionSnapshot
import pe.gob.huata.ecolactea.core.application.auth.SessionStore
import pe.gob.huata.ecolactea.shared.network.ecolacteaJson
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** AES-GCM ciphertext only; key stays non-exportable in AndroidKeyStore. Backups excluded. */
class AndroidSessionStore(context: Context) : SessionStore {
    private val file = File(context.noBackupFilesDir, "auth.session")
    private val alias = "pe.gob.huata.ecolactea.session.v1"
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256).build())
        }.generateKey()
    }
    override suspend fun read(): SessionSnapshot? = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext null
        try {
            val bytes = file.readBytes()
            require(bytes.size in 29..65536)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
            val plain = cipher.doFinal(bytes.copyOfRange(12, bytes.size))
            try { ecolacteaJson.decodeFromString<SessionSnapshot>(plain.decodeToString()) }
            finally { plain.fill(0) }
        } catch (_: Exception) { clear(); null }
    }
    override suspend fun write(session: SessionSnapshot) = withContext(Dispatchers.IO) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val plain = ecolacteaJson.encodeToString(session).encodeToByteArray()
        val encrypted = try { cipher.iv + cipher.doFinal(plain) } finally { plain.fill(0) }
        val atomic = android.util.AtomicFile(file)
        val output = atomic.startWrite()
        try { output.write(encrypted); atomic.finishWrite(output) }
        catch (failure: Exception) { atomic.failWrite(output); throw failure }
    }
    override suspend fun clear() = withContext(Dispatchers.IO) { android.util.AtomicFile(file).delete() }
}
