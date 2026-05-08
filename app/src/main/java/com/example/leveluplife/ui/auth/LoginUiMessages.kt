package com.example.leveluplife.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.leveluplife.R
import com.example.leveluplife.data.error.AuthError
import com.example.leveluplife.domain.validation.FieldError
import com.example.leveluplife.domain.validation.Validators

@Composable
fun FieldError.toMessage(): String = when (this) {
    is FieldError.Required -> stringResource(id = R.string.field_required)
    is FieldError.InvalidEmail -> stringResource(id = R.string.field_invalid_email)
    is FieldError.TooShort -> stringResource(
        id = R.string.field_min_length,
        min,
    )
    is FieldError.TooLong -> stringResource(
        id = R.string.field_max_length,
        max,
    )
}

@Composable
fun AuthError.toMessage(): String = when (this) {
    is AuthError.InvalidCredentials -> stringResource(id = R.string.login_error_invalid_credentials)
    is AuthError.AccountLocked -> stringResource(id = R.string.login_error_account_locked)
    is AuthError.BadRequest -> message?.takeIf { it.isNotBlank() }
        ?: stringResource(id = R.string.login_error_bad_request)
    is AuthError.Server -> stringResource(id = R.string.login_error_server)
    is AuthError.Network -> stringResource(id = R.string.login_error_network)
    is AuthError.Unknown -> stringResource(id = R.string.login_error_unknown)
}

fun isPasswordTooShort(input: String): Boolean =
    Validators.validatePassword(input) is FieldError.TooShort
