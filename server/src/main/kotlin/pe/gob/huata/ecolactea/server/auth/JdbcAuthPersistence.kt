package pe.gob.huata.ecolactea.server.auth

import pe.gob.huata.ecolactea.core.model.AuthenticatedUser
import pe.gob.huata.ecolactea.core.model.Role
import java.sql.Connection
import java.sql.ResultSet
import java.util.UUID
import javax.sql.DataSource

class JdbcAuthPersistence(private val dataSource: DataSource) : AuthPersistence {
    override fun <T> transaction(block: (AuthTransaction) -> T): T = dataSource.connection.use { connection ->
        connection.autoCommit = false
        try {
            val result = block(JdbcTransaction(connection))
            connection.commit()
            result
        } catch (failure: Exception) {
            connection.rollback()
            throw failure
        }
    }

    /** Explicit one-time bootstrap: never resets credentials of an existing account. */
    fun bootstrap(username: String, password: String) {
        require(username.isNotBlank() && username.length <= 100) { "Invalid bootstrap username" }
        val hash = PasswordHasher().hash(password)
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                connection.prepareStatement("SELECT id FROM app_user WHERE username = ? FOR UPDATE").use { query ->
                    query.setString(1, username)
                    query.executeQuery().use { rows ->
                        if (!rows.next()) connection.prepareStatement(
                            "INSERT INTO app_user(id, username, password_hash, role_code, active) VALUES (?, ?, ?, ?, TRUE)",
                        ).use { insert ->
                            insert.setString(1, UUID.randomUUID().toString())
                            insert.setString(2, username)
                            insert.setString(3, hash)
                            insert.setString(4, Role.ADMINISTRADOR_GENERAL.name)
                            insert.executeUpdate()
                        }
                    }
                }
                connection.commit()
            } catch (failure: Exception) { connection.rollback(); throw failure }
        }
    }
}

private class JdbcTransaction(private val connection: Connection) : AuthTransaction {
    override fun userByName(username: String) = account("username", username)
    override fun userById(id: String) = account("id", id)
    private fun account(column: String, value: String): Account? = connection.prepareStatement(
        "SELECT id, username, password_hash, role_code, active, provider_id FROM app_user WHERE $column = ? FOR UPDATE",
    ).use { query ->
        query.setString(1, value)
        query.executeQuery().use { rows ->
            if (!rows.next()) null else {
                // Unknown roles fail closed, even if the database was modified outside migrations.
                val role = Role.entries.find { it.name == rows.getString("role_code") } ?: return null
                Account(AuthenticatedUser(rows.getString("id"), rows.getString("username"), role,
                    rows.getString("provider_id")), rows.getString("password_hash"), rows.getBoolean("active"))
            }
        }
    }

    override fun session(hash: String, refresh: Boolean): StoredSession? = connection.prepareStatement(
        "SELECT * FROM auth_session WHERE ${if (refresh) "refresh_hash" else "access_hash"} = ? FOR UPDATE",
    ).use { query ->
        query.setString(1, hash)
        query.executeQuery().use { rows -> if (rows.next()) rows.session() else null }
    }

    override fun insert(session: StoredSession) {
        connection.prepareStatement(
            "INSERT INTO auth_session(id,family_id,user_id,access_hash,refresh_hash,created_at,access_expires_at,expires_at) VALUES (?,?,?,?,?,?,?,?)",
        ).use { query ->
            query.setString(1, session.id); query.setString(2, session.familyId); query.setString(3, session.userId)
            query.setString(4, session.accessHash); query.setString(5, session.refreshHash)
            query.setLong(6, session.createdAt); query.setLong(7, session.accessExpiresAt); query.setLong(8, session.expiresAt)
            query.executeUpdate()
        }
    }

    override fun revokeFamily(familyId: String, now: Long) = revoke("family_id", familyId, now)
    override fun revokeSession(id: String, now: Long) = revoke("id", id, now)
    private fun revoke(column: String, value: String, now: Long) {
        connection.prepareStatement("UPDATE auth_session SET revoked_at = ? WHERE $column = ? AND revoked_at IS NULL").use {
            it.setLong(1, now); it.setString(2, value); it.executeUpdate()
        }
    }

    private fun ResultSet.session(): StoredSession {
        val revoked = getLong("revoked_at").let { if (wasNull()) null else it }
        return StoredSession(getString("id"), getString("family_id"), getString("user_id"),
            getString("access_hash"), getString("refresh_hash"), getLong("created_at"),
            getLong("access_expires_at"), getLong("expires_at"), revoked)
    }
}
