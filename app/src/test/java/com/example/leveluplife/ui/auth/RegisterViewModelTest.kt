package com.example.leveluplife.ui.auth

import com.example.leveluplife.data.auth.AuthErrorException
import com.example.leveluplife.data.auth.AuthRepository
import com.example.leveluplife.data.auth.AuthSession
import com.example.leveluplife.data.auth.AuthUser
import com.example.leveluplife.data.auth.RegisterOutcome
import com.example.leveluplife.data.auth.RegisterValidationException
import com.example.leveluplife.data.error.AuthError
import com.example.leveluplife.domain.validation.FieldError
import com.example.leveluplife.domain.validation.RegisterFieldKey
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
class RegisterViewModelTest {

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
    fun `submit con campos vacios muestra errores requeridos y no llama al repositorio`() = runTest(testDispatcher) {
        val fake = FakeRegisterAuthRepository()
        val vm = RegisterViewModel(fake)

        vm.onSubmit()
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(FieldError.Required, state.nameError)
        assertEquals(FieldError.Required, state.emailError)
        assertEquals(FieldError.Required, state.confirmPasswordError)
        assertEquals(0, fake.registerCalls)
    }

    @Test
    fun `submit con contrasenas distintas muestra PasswordMismatch`() = runTest(testDispatcher) {
        val fake = FakeRegisterAuthRepository()
        val vm = RegisterViewModel(fake)

        fillValidForm(vm)
        vm.onConfirmPasswordChange("different")
        vm.onSubmit()
        advanceUntilIdle()

        assertEquals(FieldError.PasswordMismatch, vm.state.value.confirmPasswordError)
        assertEquals(0, fake.registerCalls)
    }

    @Test
    fun `submit valido con sesion devuelve loggedInUser`() = runTest(testDispatcher) {
        val session = AuthSession(
            accessToken = "token",
            refreshToken = null,
            user = AuthUser(id = "1", userName = "hero", level = 1, className = "Warrior"),
        )
        val fake = FakeRegisterAuthRepository(
            registerResult = Result.success(RegisterOutcome.LoggedIn(session)),
        )
        val vm = RegisterViewModel(fake)

        fillValidForm(vm)
        vm.onSubmit()
        advanceUntilIdle()

        assertNotNull(vm.state.value.loggedInUser)
        assertEquals("hero", vm.state.value.loggedInUser?.userName)
        assertEquals(1, fake.registerCalls)
    }

    @Test
    fun `registro exitoso sin auto login navega a login con mensaje`() = runTest(testDispatcher) {
        val fake = FakeRegisterAuthRepository(
            registerResult = Result.success(RegisterOutcome.LoginRequired("Usuario registrado exitosamente.")),
        )
        val vm = RegisterViewModel(fake)

        fillValidForm(vm)
        vm.onSubmit()
        advanceUntilIdle()

        assertEquals("Usuario registrado exitosamente.", vm.state.value.navigateToLoginMessage)
        assertNull(vm.state.value.loggedInUser)
    }

    @Test
    fun `error 400 del backend mapea errores de campo`() = runTest(testDispatcher) {
        val fake = FakeRegisterAuthRepository(
            registerResult = Result.failure(
                RegisterValidationException(
                    fieldErrors = mapOf(RegisterFieldKey.EMAIL to "El formato del email no es válido."),
                ),
            ),
        )
        val vm = RegisterViewModel(fake)

        fillValidForm(vm)
        vm.onSubmit()
        advanceUntilIdle()

        assertEquals(
            "El formato del email no es válido.",
            vm.state.value.serverFieldErrors[RegisterFieldKey.EMAIL],
        )
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `conflicto 409 en email muestra error en campo email`() = runTest(testDispatcher) {
        val fake = FakeRegisterAuthRepository(
            registerResult = Result.failure(
                AuthErrorException(
                    AuthError.DuplicateAccount(
                        message = "The email is already registered.",
                        field = RegisterFieldKey.EMAIL,
                    ),
                ),
            ),
        )
        val vm = RegisterViewModel(fake)

        fillValidForm(vm)
        vm.onSubmit()
        advanceUntilIdle()

        assertEquals(
            "The email is already registered.",
            vm.state.value.serverFieldErrors[RegisterFieldKey.EMAIL],
        )
    }

    @Test
    fun `onNextStep con datos personales validos avanza al paso de cuenta`() = runTest(testDispatcher) {
        val fake = FakeRegisterAuthRepository()
        val vm = RegisterViewModel(fake)

        vm.onNameChange("Ana")
        vm.onLastNameChange("García")
        vm.onBirthdateChange("1995-06-15")
        vm.onNextStep()

        assertEquals(RegisterStep.ACCOUNT, vm.state.value.step)
        assertEquals(0, fake.registerCalls)
    }

    @Test
    fun `onNextStep sin fecha muestra error y no avanza`() = runTest(testDispatcher) {
        val fake = FakeRegisterAuthRepository()
        val vm = RegisterViewModel(fake)

        vm.onNameChange("Ana")
        vm.onLastNameChange("García")
        vm.onNextStep()

        assertEquals(RegisterStep.PERSONAL, vm.state.value.step)
        assertEquals(FieldError.Required, vm.state.value.birthdateError)
    }

    private fun fillValidForm(vm: RegisterViewModel) {
        vm.onNameChange("Ana")
        vm.onLastNameChange("García")
        vm.onBirthdateChange("1995-06-15")
        vm.onNextStep()
        vm.onUserNameChange("anagarcia")
        vm.onEmailChange("ana@test.com")
        vm.onPasswordChange("secret123")
        vm.onConfirmPasswordChange("secret123")
        vm.onClassSelected(1)
    }

    private class FakeRegisterAuthRepository(
        private val registerResult: Result<RegisterOutcome> = Result.failure(IllegalStateException("not configured")),
    ) : AuthRepository {
        var registerCalls = 0

        override suspend fun login(email: String, password: String): Result<AuthSession> =
            Result.failure(IllegalStateException("not configured"))

        override suspend fun register(
            name: String,
            lastName: String,
            email: String,
            birthdate: String,
            userName: String,
            password: String,
            classId: Int,
        ): Result<RegisterOutcome> {
            registerCalls++
            return registerResult
        }

        override fun isLoggedIn(): Boolean = false
        override fun logout() = Unit
        override fun currentTokens(): Pair<String?, String?> = null to null
    }
}
