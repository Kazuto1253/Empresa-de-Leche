package pe.gob.huata.ecolactea.shared.presentation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import pe.gob.huata.ecolactea.core.application.*
import pe.gob.huata.ecolactea.core.application.auth.*
import pe.gob.huata.ecolactea.core.model.*
import kotlin.test.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AuthControllerTest {
    private class Repository(val role: Role = Role.PROVEEDOR) : AuthRepository {
        val snapshot = SessionSnapshot(AuthenticatedUser("test", "test", role), SessionToken("test-access", "test-refresh", 900))
        var gate: CompletableDeferred<Unit>? = null
        var failure: AppError? = null
        var calls = 0
        override suspend fun login(command: LoginCommand): AppResult<SessionSnapshot> {
            calls++; gate?.await()
            return failure?.let { AppResult.Failure(it) } ?: AppResult.Success(snapshot)
        }
        override suspend fun validate(accessToken: String): AppResult<AuthenticatedUser> =
            failure?.let { AppResult.Failure(it) } ?: AppResult.Success(snapshot.user)
        override suspend fun refresh(refreshToken: String): AppResult<SessionSnapshot> = AppResult.Failure(AppError.SessionRevoked)
        override suspend fun logout(refreshToken: String): AppResult<Unit> =
            failure?.let { AppResult.Failure(it) } ?: AppResult.Success(Unit)
    }
    @Test fun startupWithoutSessionReturnsLogin() = runTest {
        val controller = AuthController(Repository(), MemorySessionStore())
        assertEquals(AuthState.Starting, controller.state.value)
        controller.restore()
        assertEquals(AuthState.Login(), controller.state.value)
    }
    @Test fun loadingPreventsDoubleSubmissionAndDisplaysError() = runTest {
        val repo = Repository().apply { gate = CompletableDeferred(); failure = AppError.InvalidCredentials }
        val controller = AuthController(repo, MemorySessionStore())
        controller.restore()
        val first = launch { controller.login("test", "test-password") }
        runCurrent()
        assertEquals(AuthState.Login(loading = true), controller.state.value)
        controller.login("test", "test-password")
        assertEquals(1, repo.calls)
        repo.gate!!.complete(Unit); first.join()
        assertEquals(AuthState.Login(error = AppError.InvalidCredentials), controller.state.value)
    }
    @Test fun restoredBackendRolesHaveFourDistinctRoots() = runTest {
        val destinations = mutableSetOf<RoleDestination>()
        for (role in Role.entries) {
            val repo = Repository(role)
            val store = MemorySessionStore().apply { write(repo.snapshot) }
            val controller = AuthController(repo, store)
            controller.restore()
            val state = assertIs<AuthState.Authorized>(controller.state.value)
            assertEquals(role, state.user.role)
            destinations += RoleDestination.forRole(state.user.role)
        }
        assertEquals(RoleDestination.entries.toSet(), destinations)
    }
    @Test fun loginUsesBackendRoleAndLogoutRemovesProtectedState() = runTest {
        for (role in Role.entries) {
            val repo = Repository(role)
            val store = MemorySessionStore()
            val controller = AuthController(repo, store)
            controller.restore(); controller.login("test", "test-password")
            assertEquals(role, assertIs<AuthState.Authorized>(controller.state.value).user.role)
            controller.logout()
            assertEquals(AuthState.Login(), controller.state.value); assertNull(store.read())
            controller.restore(); assertEquals(AuthState.Login(), controller.state.value)
        }
    }
    @Test fun revocationReturnsToLoginAndOfflineLogoutReportsRemoteFailure() = runTest {
        val repo = Repository()
        val store = MemorySessionStore().apply { write(repo.snapshot) }
        val controller = AuthController(repo, store)
        controller.restore()
        repo.failure = AppError.SessionRevoked; controller.restore()
        assertEquals(AuthState.Login(), controller.state.value); assertNull(store.read())
        store.write(repo.snapshot); repo.failure = AppError.Offline
        controller.logout()
        assertNotNull(assertIs<AuthState.Login>(controller.state.value).error); assertNull(store.read())
    }
}
