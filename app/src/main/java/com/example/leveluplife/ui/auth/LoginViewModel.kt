package com.example.leveluplife.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.auth.AuthErrorException
import com.example.leveluplife.data.auth.AuthRepository
import com.example.leveluplife.data.error.AuthError
import com.example.leveluplife.domain.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.update {
            it.copy(
                email = value,
                emailError = if (it.emailError != null) Validators.validateEmail(value) else null,
                bannerError = null,
            )
        }
    }

    fun onPasswordChange(value: String) {
        _state.update {
            it.copy(
                password = value,
                passwordError = if (it.passwordError != null) Validators.validatePassword(value) else null,
                bannerError = null,
            )
        }
    }

    fun onTogglePasswordVisibility() {
        _state.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun onSubmit() {
        val current = _state.value
        if (current.isLoading) return

        val emailError = Validators.validateEmail(current.email)
        val passwordError = Validators.validatePassword(current.password)

        if (emailError != null || passwordError != null) {
            _state.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    bannerError = null,
                )
            }
            return
        }

        _state.update { it.copy(isLoading = true, bannerError = null, emailError = null, passwordError = null) }

        viewModelScope.launch {
            val result = authRepository.login(
                email = current.email.trim(),
                password = current.password,
            )
            result
                .onSuccess { session ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            loggedInUser = session.user,
                            bannerError = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    val authError = (throwable as? AuthErrorException)?.authError
                        ?: AuthError.Unknown(throwable.message)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            bannerError = authError,
                        )
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

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(LoginViewModel::class.java))
            return LoginViewModel(authRepository) as T
        }
    }
}
