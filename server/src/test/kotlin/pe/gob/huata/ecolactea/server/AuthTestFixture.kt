package pe.gob.huata.ecolactea.server

import pe.gob.huata.ecolactea.server.auth.*
import pe.gob.huata.ecolactea.core.model.*
import java.time.*
import java.util.UUID

class TestClock(var seconds: Long = 1000) : Clock() {
    override fun instant(): Instant = Instant.ofEpochSecond(seconds)
    override fun getZone(): ZoneId = ZoneOffset.UTC
    override fun withZone(zone: ZoneId): Clock = this
}

class MemoryAuthPersistence : AuthPersistence, AuthTransaction {
    val accounts = mutableMapOf<String, Account>()
    val sessions = mutableMapOf<String, StoredSession>()
    @Synchronized override fun <T> transaction(block: (AuthTransaction) -> T): T = block(this)
    override fun userByName(username: String) = accounts.values.find { it.user.username == username }
    override fun userById(id: String) = accounts[id]
    override fun session(hash: String, refresh: Boolean) = sessions.values.find {
        if (refresh) it.refreshHash == hash else it.accessHash == hash
    }
    override fun insert(session: StoredSession) { sessions[session.id] = session }
    override fun revokeFamily(familyId: String, now: Long) {
        sessions.values.filter { it.familyId == familyId }.toList().forEach { revokeSession(it.id, now) }
    }
    override fun revokeSession(id: String, now: Long) { sessions[id]?.let { sessions[id] = it.copy(revokedAt = it.revokedAt ?: now) } }
}

class AuthFixture {
    val password = UUID.randomUUID().toString()
    val hash = PasswordHasher().hash(password)
    val db = MemoryAuthPersistence()
    val clock = TestClock()
    val service = AuthService(db, clock = clock)
    init {
        Role.entries.forEach { role ->
            val user = AuthenticatedUser(role.name, role.name, role, if (role == Role.PROVEEDOR) "provider-own" else null)
            db.accounts[user.userId] = Account(user, hash, true)
        }
    }
    fun block(role: Role) {
        val old = db.accounts.getValue(role.name)
        db.accounts[role.name] = Account(old.user, old.passwordHash, false)
    }
}
