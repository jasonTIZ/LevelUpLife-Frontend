package com.example.leveluplife.data.auth

import com.example.leveluplife.domain.validation.RegisterFieldKey

sealed class RegisterOutcome {
    data class LoggedIn(val session: AuthSession) : RegisterOutcome()
    data class LoginRequired(val message: String) : RegisterOutcome()
}

class RegisterValidationException(
    val fieldErrors: Map<RegisterFieldKey, String>,
    override val message: String? = null,
) : Exception(message)
