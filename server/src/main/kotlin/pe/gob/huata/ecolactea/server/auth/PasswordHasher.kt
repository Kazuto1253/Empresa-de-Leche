package pe.gob.huata.ecolactea.server.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** PBKDF2 is provided by JCA, not a custom cryptographic implementation. */
class PasswordHasher {
    fun hash(password: String): String {
        require(password.isNotBlank() && password.length in 12..256)
        val salt = ByteArray(16).also(SecureRandom()::nextBytes)
        return listOf("pbkdf2-sha256", "600000", encode(salt), encode(derive(password, salt, 600000))).joinToString("$")
    }

    fun verify(password: String, encoded: String): Boolean = try {
        val parts = encoded.split('$')
        if (parts.size != 4 || parts[0] != "pbkdf2-sha256" || parts[1] != "600000") false
        else {
            val salt = Base64.getDecoder().decode(parts[2])
            val expected = Base64.getDecoder().decode(parts[3])
            salt.size == 16 && expected.size == 32 &&
                MessageDigest.isEqual(expected, derive(password, salt, 600000))
        }
    } catch (_: IllegalArgumentException) { false }

    private fun derive(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val chars = password.toCharArray()
        val spec = PBEKeySpec(chars, salt, iterations, 256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded }
        finally { spec.clearPassword(); chars.fill('\u0000') }
    }
    private fun encode(bytes: ByteArray) = Base64.getEncoder().encodeToString(bytes)
}
