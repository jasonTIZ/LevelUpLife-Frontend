package com.example.leveluplife.ui.auth

import com.example.leveluplife.data.auth.AuthUser
import com.example.leveluplife.data.error.AuthError
import com.example.leveluplife.domain.validation.FieldError
import com.example.leveluplife.domain.validation.RegisterFieldKey

enum class RegisterStep {
    PERSONAL,
    ACCOUNT,
}

data class RegisterUiState(
    val step: RegisterStep = RegisterStep.PERSONAL,
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    val birthdate: String = "",
    val userName: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val classId: Int? = CharacterClasses.available.firstOrNull()?.id,
    val nameError: FieldError? = null,
    val lastNameError: FieldError? = null,
    val emailError: FieldError? = null,
    val birthdateError: FieldError? = null,
    val userNameError: FieldError? = null,
    val passwordError: FieldError? = null,
    val confirmPasswordError: FieldError? = null,
    val classIdError: FieldError? = null,
    val serverFieldErrors: Map<RegisterFieldKey, String> = emptyMap(),
    val isLoading: Boolean = false,
    val bannerError: AuthError? = null,
    val loggedInUser: AuthUser? = null,
    val navigateToLoginMessage: String? = null,
)
