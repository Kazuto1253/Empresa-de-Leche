package pe.gob.huata.ecolactea.core.application.auth

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import pe.gob.huata.ecolactea.core.application.*
import pe.gob.huata.ecolactea.core.model.*
import kotlin.test.*

class AuthUseCasesTest {
    private fun snapshot(role: Role = Role.PROVEEDOR) = SessionSnapshot(
        AuthenticatedUser("test-user", "test", role, "own-provider", Authorization.permissions(role)),
        SessionToken("test-access", "test-refresh", 900))

    private class Repository(var value: SessionSnapshot) : AuthRepository {
        var loginError: AppError? = null
        var validateError: AppError? = null
        var refreshError: AppError? = null
        var loginCalls = 0
        var refreshCalls = 0
        var logoutCalls = 0
        override suspend fun login(command: LoginCommand): AppResult<SessionSnapshot> {
            loginCalls++
            return loginError?.let { AppResult.Failure(it) } ?: AppResult.Success(value)
        }
        override suspend fun refresh(refreshToken: String): AppResult<SessionSnapshot> {
            refreshCalls++
            return refreshError?.let { AppResult.Failure(it) } ?: AppResult.Success(value)
        }
        override suspend fun validate(accessToken: String): AppResult<AuthenticatedUser> =
            validateError?.let { AppResult.Failure(it) } ?: AppResult.Success(value.user)
        override suspend fun logout(refreshToken: String): AppResult<Unit> { logoutCalls++; return AppResult.Success(Unit) }
    }

    @Test fun emptyFieldsNeverReachBackend() = runTest {
        val repo = Repository(snapshot())
        val login = LoginUseCase(repo, MemorySessionStore())
        for (command in listOf(LoginCommand("", "test-password"), LoginCommand("test", ""))) {
            assertIs<AppError.Validation>(assertIs<AppResult.Failure>(login(command)).error)
        }
        assertEquals(0, repo.loginCalls)
    }
    @Test fun invalidCredentialsAndBlockedAccountStayUnauthenticated() = runTest {
        val store = MemorySessionStore()
        val repo = Repository(snapshot())
        for (error in listOf(AppError.InvalidCredentials, AppError.AccountBlocked)) {
            repo.loginError = error
            assertEquals(AppResult.Failure(error), LoginUseCase(repo, store)(LoginCommand("test", "test-password")))
            assertNull(store.read())
        }
    }
    @Test fun loginPersistsOnlyBackendIdentityForEachRole() = runTest {
        for (role in Role.entries) {
            val store = MemorySessionStore()
            val value = snapshot(role)
            assertEquals(AppResult.Success(value), LoginUseCase(Repository(value), store)(LoginCommand("test", "test-password")))
            assertEquals(value, store.read())
        }
    }
    @Test fun unknownRoleIsRejected() {
        assertFailsWith<SerializationException> { Json.decodeFromString<Role>("\"SUPERUSER\"") }
    }
    @Test fun expiredAccessRefreshesAndSavesRotatedSession() = runTest {
        val store = MemorySessionStore().apply { write(snapshot()) }
        val repo = Repository(snapshot().copy(token = SessionToken("rotated-access", "rotated-refresh", 1800)))
        repo.validateError = AppError.SessionExpired
        assertEquals(StartupDestination.Authorized(repo.value.user), RestoreSessionUseCase(store, repo)())
        assertEquals(repo.value, store.read())
        assertEquals(1, repo.refreshCalls)
    }
    @Test fun revokedAndBlockedSessionsAreClearedWithoutRefresh() = runTest {
        for (error in listOf(AppError.SessionRevoked, AppError.AccountBlocked)) {
            val store = MemorySessionStore().apply { write(snapshot()) }
            val repo = Repository(snapshot()).apply { validateError = error }
            assertEquals(StartupDestination.Login, RestoreSessionUseCase(store, repo)())
            assertNull(store.read()); assertEquals(0, repo.refreshCalls)
        }
    }
    @Test fun expiredRefreshClearsSession() = runTest {
        val store = MemorySessionStore().apply { write(snapshot()) }
        val repo = Repository(snapshot()).apply { validateError = AppError.SessionExpired; refreshError = AppError.SessionExpired }
        assertEquals(StartupDestination.Login, RestoreSessionUseCase(store, repo)())
        assertNull(store.read())
    }
    @Test fun offlineDoesNotDestroySessionOrAuthorizeIt() = runTest {
        val store = MemorySessionStore().apply { write(snapshot()) }
        val repo = Repository(snapshot()).apply { validateError = AppError.Offline }
        assertEquals(StartupDestination.Unavailable(AppError.Offline), RestoreSessionUseCase(store, repo)())
        assertNotNull(store.read()); assertEquals(0, repo.refreshCalls)
    }
    @Test fun logoutRevokesAndClears() = runTest {
        val store = MemorySessionStore().apply { write(snapshot()) }
        val repo = Repository(snapshot())
        LogoutUseCase(repo, store)()
        assertEquals(1, repo.logoutCalls); assertNull(store.read())
    }
    @Test fun authorizationDeniesEscalationAndOtherProvider() {
        val user = snapshot().user
        assertTrue(Authorization.allows(user, Permission.PROVIDER_HOME))
        assertFalse(Authorization.allows(user.copy(permissions = setOf(Permission.MANAGE_USERS)), Permission.MANAGE_USERS))
        assertTrue(Authorization.ownsProvider(user, "own-provider"))
        assertFalse(Authorization.ownsProvider(user, "another-provider"))
        assertFalse(Authorization.ownsProvider(user.copy(providerId = null), ""))
    }
}
