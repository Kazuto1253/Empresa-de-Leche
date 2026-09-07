package pe.gob.huata.ecolactea.server.auth

import pe.gob.huata.ecolactea.core.application.AppError
import pe.gob.huata.ecolactea.core.application.AppResult
import pe.gob.huata.ecolactea.core.application.auth.*
import pe.gob.huata.ecolactea.core.model.*
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Clock
import java.util.Base64
import java.util.UUID

class AuthService(
    private val persistence: AuthPersistence,
    private val passwords: PasswordHasher = PasswordHasher(),
    private val clock: Clock = Clock.systemUTC(),
) {
    private val random = SecureRandom()
    private val dummyHash = passwords.hash(UUID.randomUUID().toString())

    fun login(command: LoginCommand): AppResult<SessionSnapshot> {
        if (command.username.isBlank() || command.username.length > 100 ||
            command.password.isBlank() || command.password.length > 256) {
            return AppResult.Failure(AppError.Validation(mapOf("credentials" to "Completa usuario y contraseña")))
        }
        return persistence.transaction { tx ->
            val account = tx.userByName(command.username.trim())
            val valid = passwords.verify(command.password, account?.passwordHash ?: dummyHash)
            if (!valid || account == null) AppResult.Failure(AppError.InvalidCredentials)
            else if (!account.active) AppResult.Failure(AppError.AccountBlocked)
            else issue(tx, account, UUID.randomUUID().toString(), clock.instant().epochSecond + 604800)
        }
    }

    fun refresh(token: String): AppResult<SessionSnapshot> {
        if (!validToken(token)) return AppResult.Failure(AppError.Unauthorized)
        return persistence.transaction { tx ->
            val session = tx.session(digest(token), true)
                ?: return@transaction AppResult.Failure(AppError.Unauthorized)
            val now = clock.instant().epochSecond
            if (session.revokedAt != null) {
                tx.revokeFamily(session.familyId, now)
                return@transaction AppResult.Failure(AppError.SessionRevoked)
            }
            if (session.expiresAt <= now) return@transaction AppResult.Failure(AppError.SessionExpired)
            val account = tx.userById(session.userId)
            if (account == null || !account.active) {
                tx.revokeFamily(session.familyId, now)
                return@transaction AppResult.Failure(AppError.AccountBlocked)
            }
            tx.revokeSession(session.id, now)
            issue(tx, account, session.familyId, session.expiresAt)
        }
    }

    fun me(token: String): AppResult<AuthenticatedUser> {
        if (!validToken(token)) return AppResult.Failure(AppError.Unauthorized)
        return persistence.transaction { tx ->
            val session = tx.session(digest(token), false)
                ?: return@transaction AppResult.Failure(AppError.Unauthorized)
            val now = clock.instant().epochSecond
            if (session.revokedAt != null) return@transaction AppResult.Failure(AppError.SessionRevoked)
            if (session.accessExpiresAt <= now) return@transaction AppResult.Failure(AppError.SessionExpired)
            val account = tx.userById(session.userId)
            if (account == null || !account.active) {
                tx.revokeFamily(session.familyId, now)
                AppResult.Failure(AppError.AccountBlocked)
            } else AppResult.Success(identity(account))
        }
    }

    fun authorize(token: String, permission: Permission, providerId: String? = null): AppResult<Unit> =
        when (val result = me(token)) {
            is AppResult.Failure -> result
            is AppResult.Success -> if (Authorization.allows(result.value, permission) &&
                (permission != Permission.PROVIDER_HOME ||
                    (providerId != null && Authorization.ownsProvider(result.value, providerId)))) {
                AppResult.Success(Unit)
            } else AppResult.Failure(AppError.Forbidden)
        }

    fun logout(token: String): AppResult<Unit> {
        if (validToken(token)) persistence.transaction { tx ->
            tx.session(digest(token), true)?.let { tx.revokeFamily(it.familyId, clock.instant().epochSecond) }
        }
        return AppResult.Success(Unit)
    }

    private fun identity(account: Account) = account.user.copy(permissions = Authorization.permissions(account.user.role))

    private fun issue(tx: AuthTransaction, account: Account, family: String, expiry: Long): AppResult<SessionSnapshot> {
        val access = token()
        val refresh = token()
        val now = clock.instant().epochSecond
        val accessExpiry = minOf(now + 900, expiry)
        tx.insert(StoredSession(UUID.randomUUID().toString(), family, account.user.userId,
            digest(access), digest(refresh), now, accessExpiry, expiry))
        return AppResult.Success(SessionSnapshot(identity(account), SessionToken(access, refresh, accessExpiry)))
    }

    private fun token() = Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(32).also(random::nextBytes))
    private fun validToken(token: String) = token.matches(Regex("[A-Za-z0-9_-]{43}"))
    private fun digest(token: String) = MessageDigest.getInstance("SHA-256")
        .digest(token.toByteArray(Charsets.US_ASCII)).joinToString("") { "%02x".format(it) }
}
