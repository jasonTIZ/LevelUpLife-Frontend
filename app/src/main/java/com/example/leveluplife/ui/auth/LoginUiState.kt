package com.example.leveluplife.ui.auth

import com.example.leveluplife.data.auth.AuthUser
import com.example.leveluplife.data.error.AuthError
import com.example.leveluplife.domain.validation.FieldError

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val emailError: FieldError? = null,
    val passwordError: FieldError? = null,
    val isLoading: Boolean = false,
    val bannerError: AuthError? = null,
    val loggedInUser: AuthUser? = null,
)
