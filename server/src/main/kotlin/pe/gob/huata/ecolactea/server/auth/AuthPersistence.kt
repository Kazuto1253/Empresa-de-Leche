package pe.gob.huata.ecolactea.server.auth

import pe.gob.huata.ecolactea.core.model.AuthenticatedUser

class Account(val user: AuthenticatedUser, val passwordHash: String, val active: Boolean)

data class StoredSession(
    val id: String, val familyId: String, val userId: String,
    val accessHash: String, val refreshHash: String,
    val createdAt: Long, val accessExpiresAt: Long, val expiresAt: Long, val revokedAt: Long? = null,
)

interface AuthTransaction {
    fun userByName(username: String): Account?
    fun userById(id: String): Account?
    fun session(hash: String, refresh: Boolean): StoredSession?
    fun insert(session: StoredSession)
    fun revokeFamily(familyId: String, now: Long)
    fun revokeSession(id: String, now: Long)
}

interface AuthPersistence {
    /** Atomic, serialized session rotation, including revocation on replay. */
    fun <T> transaction(block: (AuthTransaction) -> T): T
}
