package com.example.leveluplife.ui.auth

import com.example.leveluplife.data.auth.AuthErrorException
import com.example.leveluplife.data.auth.AuthRepository
import com.example.leveluplife.data.auth.AuthSession
import com.example.leveluplife.data.auth.AuthUser
import com.example.leveluplife.data.error.AuthError
import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.network.dto.CreateHabitRequestDto
import com.example.leveluplife.data.network.dto.CreateHabitResponseDto
import com.example.leveluplife.data.network.dto.HabitDto
import com.example.leveluplife.data.network.dto.HabitsPageResponse
import com.example.leveluplife.data.network.dto.UpdateHabitRequestDto
import com.example.leveluplife.domain.validation.FieldError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `password change elimina espacios al escribir`() = runTest(testDispatcher) {
        val fake = FakeAuthRepository()
        val vm = LoginViewModel(fake, FakeHabitRepository())

        vm.onPasswordChange("123 456")
        assertEquals("123456", vm.state.value.password)
    }

    @Test
    fun `submit con identificador invalido no llama al repositorio y muestra error de campo`() = runTest(testDispatcher) {
        val fake = FakeAuthRepository()
        val vm = LoginViewModel(fake, FakeHabitRepository())

        vm.onEmailChange("bad email!")
        vm.onPasswordChange("123456")
        vm.onSubmit()

        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(FieldError.InvalidUserNameOrEmail, state.emailError)
        assertNull(state.passwordError)
        assertFalse(state.isLoading)
        assertEquals(0, fake.loginCalls)
    }

    @Test
    fun `submit con password vacia muestra Required y no llama repositorio`() = runTest(testDispatcher) {
        val fake = FakeAuthRepository()
        val vm = LoginViewModel(fake, FakeHabitRepository())

        vm.onEmailChange("darwin@levelup.life")
        vm.onPasswordChange("")
        vm.onSubmit()

        advanceUntilIdle()

        val state = vm.state.value
        assertNull(state.emailError)
        assertEquals(FieldError.Required, state.passwordError)
        assertEquals(0, fake.loginCalls)
    }

    @Test
    fun `submit valido con respuesta exitosa setea loggedInUser y persiste tokens`() = runTest(testDispatcher) {
        val session = AuthSession(
            accessToken = "access-123",
            refreshToken = "refresh-456",
            user = AuthUser(
                id = "1",
                userName = "Darwin",
                level = 5,
                className = "Warrior",
            ),
        )
        val fake = FakeAuthRepository(result = Result.success(session))
        val vm = LoginViewModel(fake, FakeHabitRepository())

        vm.onEmailChange("darwin@levelup.life")
        vm.onPasswordChange("123456")

        // Tras onSubmit() el estado se vuelve isLoading=true sincrónicamente.
        vm.onSubmit()
        assertTrue(vm.state.value.isLoading)

        advanceUntilIdle()

        val finalState = vm.state.value
        assertFalse(finalState.isLoading)
        assertNull(finalState.bannerError)
        assertNotNull(finalState.loggedInUser)
        assertEquals("Darwin", finalState.loggedInUser?.userName)
        assertEquals(1, fake.loginCalls)
        assertEquals("access-123" to "refresh-456", fake.currentTokens())
    }

    @Test
    fun `submit valido con InvalidCredentials muestra banner y no setea user`() = runTest(testDispatcher) {
        val fake = FakeAuthRepository(
            result = Result.failure(AuthErrorException(AuthError.InvalidCredentials())),
        )
        val vm = LoginViewModel(fake, FakeHabitRepository())

        vm.onEmailChange("darwin@levelup.life")
        vm.onPasswordChange("123456")
        vm.onSubmit()

        advanceUntilIdle()

        val state = vm.state.value
        assertFalse(state.isLoading)
        assertTrue(state.bannerError is AuthError.InvalidCredentials)
        assertNull(state.loggedInUser)
        assertEquals(null to null, fake.currentTokens())
    }

    @Test
    fun `submit con error de red muestra banner Network y permite reintentar`() = runTest(testDispatcher) {
        val networkResult = Result.failure<AuthSession>(AuthErrorException(AuthError.Network()))
        val fake = FakeAuthRepository(result = networkResult)
        val vm = LoginViewModel(fake, FakeHabitRepository())

        vm.onEmailChange("darwin@levelup.life")
        vm.onPasswordChange("123456")
        vm.onSubmit()
        advanceUntilIdle()
        assertTrue(vm.state.value.bannerError is AuthError.Network)

        // Reintentar (segunda vez tiene éxito)
        fake.nextResult = Result.success(
            AuthSession(
                accessToken = "ok",
                refreshToken = null,
                user = AuthUser(id = "1", userName = "name", level = 1, className = "Warrior"),
            ),
        )
        vm.onSubmit()
        advanceUntilIdle()

        val state = vm.state.value
        assertNull(state.bannerError)
        assertNotNull(state.loggedInUser)
        assertEquals(2, fake.loginCalls)
    }

    private class FakeAuthRepository(
        result: Result<AuthSession> = Result.failure(IllegalStateException("not configured")),
    ) : AuthRepository {
        var nextResult: Result<AuthSession> = result
        var loginCalls: Int = 0
        private var access: String? = null
        private var refresh: String? = null

        override suspend fun login(email: String, password: String): Result<AuthSession> {
            loginCalls++
            val r = nextResult
            r.onSuccess {
                access = it.accessToken
                refresh = it.refreshToken
            }
            return r
        }

        override suspend fun register(
            name: String,
            lastName: String,
            email: String,
            birthdate: String,
            userName: String,
            password: String,
            classId: Int,
        ): Result<com.example.leveluplife.data.auth.RegisterOutcome> =
            Result.failure(IllegalStateException("not configured"))

        override fun isLoggedIn(): Boolean = !access.isNullOrBlank()

        override suspend fun logout() {
            access = null
            refresh = null
        }

        override suspend fun clearSessionAfterAccountDeactivation(message: String?) {
            access = null
            refresh = null
        }

        override fun clearLocalSession() {
            access = null
            refresh = null
        }

        override fun currentTokens(): Pair<String?, String?> = access to refresh
    }

    private class FakeHabitRepository : HabitRepository {
        private var currentUserId = 1

        override suspend fun getActiveHabits(page: Int, pageSize: Int): Result<HabitsPageResponse> =
            Result.failure(IllegalStateException("not configured"))

        override suspend fun createHabit(request: CreateHabitRequestDto): Result<CreateHabitResponseDto> =
            Result.failure(IllegalStateException("not configured"))

        override suspend fun updateHabit(request: UpdateHabitRequestDto): Result<CreateHabitResponseDto> =
            Result.failure(IllegalStateException("not configured"))

        override suspend fun getHabitById(id: Int): Result<HabitDto> =
            Result.failure(IllegalStateException("not configured"))

        override fun setCurrentUserId(userId: Int) {
            currentUserId = userId
        }

        override fun getCurrentUserId(): Int = currentUserId
    }
}
