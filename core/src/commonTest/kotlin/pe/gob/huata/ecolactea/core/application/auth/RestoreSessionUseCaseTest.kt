package pe.gob.huata.ecolactea.core.application.auth

import kotlinx.coroutines.test.runTest
import pe.gob.huata.ecolactea.core.application.AppError
import pe.gob.huata.ecolactea.core.application.AppResult
import pe.gob.huata.ecolactea.core.model.AuthenticatedUser
import pe.gob.huata.ecolactea.core.model.Role
import kotlin.test.Test
import kotlin.test.assertEquals

class RestoreSessionUseCaseTest {
    private val user = AuthenticatedUser("u-1", "acopiador.demo", Role.ACOPIADOR)
    private val snapshot = SessionSnapshot(user, SessionToken("access", "refresh", 1L))

    @Test
    fun missingSecureSessionRoutesToLogin() = runTest {
        val store = FakeStore(null)

        assertEquals(StartupDestination.Login, RestoreSessionUseCase(store, FakeAuthRepository())())
    }

    @Test
    fun validStoredSessionRestoresTheBackendRole() = runTest {
        val store = FakeStore(snapshot)

        assertEquals(StartupDestination.Authorized(user), RestoreSessionUseCase(store, FakeAuthRepository())())
    }

    @Test
    fun invalidStoredSessionIsClearedWhenRefreshFails() = runTest {
        val store = FakeStore(snapshot)

        RestoreSessionUseCase(store, FakeAuthRepository(validateSucceeds = false))()

        assertEquals(null, store.value)
    }

    private class FakeStore(var value: SessionSnapshot?) : SessionStore {
        override suspend fun read(): SessionSnapshot? = value
        override suspend fun write(session: SessionSnapshot) {
            value = session
        }
        override suspend fun clear() {
            value = null
        }
    }

    private class FakeAuthRepository(
        private val validateSucceeds: Boolean = true,
    ) : AuthRepository {
        override suspend fun login(command: LoginCommand): AppResult<SessionSnapshot> =
            AppResult.Failure(AppError.Unauthorized)

        override suspend fun refresh(refreshToken: String): AppResult<SessionSnapshot> =
            AppResult.Failure(AppError.Unauthorized)

        override suspend fun validate(accessToken: String): AppResult<AuthenticatedUser> =
            if (validateSucceeds) {
                AppResult.Success(AuthenticatedUser("u-1", "acopiador.demo", Role.ACOPIADOR))
            } else {
                AppResult.Failure(AppError.Unauthorized)
            }

        override suspend fun logout(refreshToken: String): AppResult<Unit> =
            AppResult.Success(Unit)
    }
}
