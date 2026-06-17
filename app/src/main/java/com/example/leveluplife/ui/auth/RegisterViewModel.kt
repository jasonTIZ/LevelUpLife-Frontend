package com.example.leveluplife.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.auth.AuthErrorException
import com.example.leveluplife.data.auth.AuthRepository
import com.example.leveluplife.data.auth.RegisterOutcome
import com.example.leveluplife.data.auth.RegisterValidationException
import com.example.leveluplife.data.error.AuthError
import com.example.leveluplife.domain.validation.ProfileInputSanitizer
import com.example.leveluplife.domain.validation.RegisterFieldKey
import com.example.leveluplife.domain.validation.RegistrationValidators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onNameChange(value: String) {
        val sanitized = ProfileInputSanitizer.sanitizePersonName(value)
        _state.update {
            it.copy(
                name = sanitized,
                nameError = if (it.nameError != null || it.serverFieldErrors.containsKey(RegisterFieldKey.NAME)) {
                    RegistrationValidators.validateName(sanitized)
                } else {
                    null
                },
                serverFieldErrors = it.serverFieldErrors - RegisterFieldKey.NAME,
                bannerError = null,
            )
        }
    }

    fun onLastNameChange(value: String) {
        val sanitized = ProfileInputSanitizer.sanitizePersonName(value)
        _state.update {
            it.copy(
                lastName = sanitized,
                lastNameError = if (it.lastNameError != null || it.serverFieldErrors.containsKey(RegisterFieldKey.LAST_NAME)) {
                    RegistrationValidators.validateLastName(sanitized)
                } else {
                    null
                },
                serverFieldErrors = it.serverFieldErrors - RegisterFieldKey.LAST_NAME,
                bannerError = null,
            )
        }
    }

    fun onEmailChange(value: String) {
        val sanitized = ProfileInputSanitizer.sanitizeEmail(value)
        _state.update {
            it.copy(
                email = sanitized,
                emailError = if (it.emailError != null || it.serverFieldErrors.containsKey(RegisterFieldKey.EMAIL)) {
                    RegistrationValidators.validateEmail(sanitized)
                } else {
                    null
                },
                serverFieldErrors = it.serverFieldErrors - RegisterFieldKey.EMAIL,
                bannerError = null,
            )
        }
    }

    fun onBirthdateChange(value: String) {
        val sanitized = ProfileInputSanitizer.sanitizeBirthdate(value)
        _state.update {
            it.copy(
                birthdate = sanitized,
                birthdateError = if (it.birthdateError != null || it.serverFieldErrors.containsKey(RegisterFieldKey.BIRTHDATE)) {
                    RegistrationValidators.validateBirthdate(sanitized)
                } else {
                    null
                },
                serverFieldErrors = it.serverFieldErrors - RegisterFieldKey.BIRTHDATE,
                bannerError = null,
            )
        }
    }

    fun onUserNameChange(value: String) {
        val sanitized = ProfileInputSanitizer.sanitizeUserName(value)
        _state.update { current ->
            current.copy(
                userName = sanitized,
                userNameError = if (current.userNameError != null || current.serverFieldErrors.containsKey(RegisterFieldKey.USER_NAME)) {
                    RegistrationValidators.validateUserName(sanitized, current.name)
                } else {
                    null
                },
                serverFieldErrors = current.serverFieldErrors - RegisterFieldKey.USER_NAME,
                bannerError = null,
            )
        }
    }

    fun onPasswordChange(value: String) {
        _state.update { current ->
            current.copy(
                password = value,
                passwordError = if (current.passwordError != null || current.serverFieldErrors.containsKey(RegisterFieldKey.PASSWORD)) {
                    RegistrationValidators.validatePassword(value)
                } else {
                    null
                },
                confirmPasswordError = if (current.confirmPasswordError != null) {
                    RegistrationValidators.validateConfirmPassword(value, current.confirmPassword)
                } else {
                    null
                },
                serverFieldErrors = current.serverFieldErrors - RegisterFieldKey.PASSWORD,
                bannerError = null,
            )
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _state.update { current ->
            current.copy(
                confirmPassword = value,
                confirmPasswordError = if (current.confirmPasswordError != null) {
                    RegistrationValidators.validateConfirmPassword(current.password, value)
                } else {
                    null
                },
                bannerError = null,
            )
        }
    }

    fun onTogglePasswordVisibility() {
        _state.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun onToggleConfirmPasswordVisibility() {
        _state.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }
    }

    fun onClassSelected(classId: Int) {
        _state.update {
            it.copy(
                classId = classId,
                classIdError = null,
                serverFieldErrors = it.serverFieldErrors - RegisterFieldKey.CLASS_ID,
                bannerError = null,
            )
        }
    }

    fun onNextStep() {
        val current = _state.value
        if (current.isLoading || current.step != RegisterStep.PERSONAL) return

        val nameError = RegistrationValidators.validateName(current.name)
        val lastNameError = RegistrationValidators.validateLastName(current.lastName)
        val birthdateError = RegistrationValidators.validateBirthdate(current.birthdate)

        if (nameError != null || lastNameError != null || birthdateError != null) {
            _state.update {
                it.copy(
                    nameError = nameError,
                    lastNameError = lastNameError,
                    birthdateError = birthdateError,
                    bannerError = null,
                )
            }
            return
        }

        _state.update {
            it.copy(
                step = RegisterStep.ACCOUNT,
                nameError = null,
                lastNameError = null,
                birthdateError = null,
                bannerError = null,
            )
        }
    }

    fun onPreviousStep() {
        _state.update {
            it.copy(
                step = RegisterStep.PERSONAL,
                bannerError = null,
            )
        }
    }

    fun onSubmit() {
        val current = _state.value
        if (current.isLoading) return

        val nameError = RegistrationValidators.validateName(current.name)
        val lastNameError = RegistrationValidators.validateLastName(current.lastName)
        val emailError = RegistrationValidators.validateEmail(current.email)
        val birthdateError = RegistrationValidators.validateBirthdate(current.birthdate)
        val userNameError = RegistrationValidators.validateUserName(current.userName, current.name)
        val passwordError = RegistrationValidators.validatePassword(current.password)
        val confirmPasswordError = RegistrationValidators.validateConfirmPassword(
            current.password,
            current.confirmPassword,
        )
        val classIdError = RegistrationValidators.validateClassId(current.classId)

        if (listOf(
                nameError,
                lastNameError,
                emailError,
                birthdateError,
                userNameError,
                passwordError,
                confirmPasswordError,
                classIdError,
            ).any { it != null }
        ) {
            _state.update {
                it.copy(
                    nameError = nameError,
                    lastNameError = lastNameError,
                    emailError = emailError,
                    birthdateError = birthdateError,
                    userNameError = userNameError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError,
                    classIdError = classIdError,
                    bannerError = null,
                    serverFieldErrors = emptyMap(),
                )
            }
            return
        }

        _state.update {
            it.copy(
                isLoading = true,
                bannerError = null,
                nameError = null,
                lastNameError = null,
                emailError = null,
                birthdateError = null,
                userNameError = null,
                passwordError = null,
                confirmPasswordError = null,
                classIdError = null,
                serverFieldErrors = emptyMap(),
            )
        }

        viewModelScope.launch {
            val result = authRepository.register(
                name = current.name,
                lastName = current.lastName,
                email = current.email,
                birthdate = current.birthdate,
                userName = current.userName,
                password = current.password,
                classId = current.classId!!,
            )
            result
                .onSuccess { outcome ->
                    when (outcome) {
                        is RegisterOutcome.LoggedIn -> _state.update {
                            it.copy(
                                isLoading = false,
                                loggedInUser = outcome.session.user,
                                bannerError = null,
                            )
                        }
                        is RegisterOutcome.LoginRequired -> _state.update {
                            it.copy(
                                isLoading = false,
                                navigateToLoginMessage = outcome.message,
                                bannerError = null,
                            )
                        }
                    }
                }
                .onFailure { throwable ->
                    when (throwable) {
                        is RegisterValidationException -> {
                            val personalKeys = setOf(
                                RegisterFieldKey.NAME,
                                RegisterFieldKey.LAST_NAME,
                                RegisterFieldKey.BIRTHDATE,
                            )
                            val goToPersonal = throwable.fieldErrors.keys.any { it in personalKeys }
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    step = if (goToPersonal) RegisterStep.PERSONAL else RegisterStep.ACCOUNT,
                                    serverFieldErrors = throwable.fieldErrors,
                                    bannerError = if (throwable.fieldErrors.isEmpty()) {
                                        AuthError.BadRequest(throwable.message)
                                    } else {
                                        null
                                    },
                                )
                            }
                        }
                        is AuthErrorException -> {
                            val authError = throwable.authError
                            if (authError is AuthError.DuplicateAccount && authError.field != null) {
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        serverFieldErrors = mapOf(
                                            authError.field to (authError.message ?: ""),
                                        ),
                                        bannerError = null,
                                    )
                                }
                            } else {
                                _state.update {
                                    it.copy(isLoading = false, bannerError = authError)
                                }
                            }
                        }
                        else -> _state.update {
                            it.copy(
                                isLoading = false,
                                bannerError = AuthError.Unknown(throwable.message),
                            )
                        }
                    }
                }
        }
    }

    fun dismissError() {
        _state.update { it.copy(bannerError = null) }
    }

    fun consumeLoggedIn() {
        _state.update { it.copy(loggedInUser = null) }
    }

    fun consumeNavigateToLogin() {
        _state.update { it.copy(navigateToLoginMessage = null) }
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(RegisterViewModel::class.java))
            return RegisterViewModel(authRepository) as T
        }
    }
}
