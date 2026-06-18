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
    is FieldError.InvalidUserNameOrEmail -> stringResource(id = R.string.field_invalid_username_or_email)
    is FieldError.InvalidEmail -> stringResource(id = R.string.field_invalid_email)
    is FieldError.InvalidBirthdate -> stringResource(id = R.string.field_invalid_birthdate)
    is FieldError.InvalidCharacters -> stringResource(id = R.string.field_invalid_characters)
    is FieldError.TooShort -> stringResource(
        id = R.string.field_min_length,
        min,
    )
    is FieldError.TooLong -> stringResource(
        id = R.string.field_max_length,
        max,
    )
    is FieldError.PasswordMismatch -> stringResource(id = R.string.field_password_mismatch)
    is FieldError.SameAsName -> stringResource(id = R.string.field_username_same_as_name)
}

@Composable
fun AuthError.toRegisterMessage(): String = when (this) {
    is AuthError.DuplicateAccount -> message?.takeIf { it.isNotBlank() }
        ?: stringResource(id = R.string.register_error_duplicate_account)
    else -> toMessage()
}

@Composable
fun AuthError.toMessage(): String = when (this) {
    is AuthError.InvalidCredentials -> stringResource(id = R.string.login_error_invalid_credentials)
    is AuthError.AccountLocked -> stringResource(id = R.string.login_error_account_locked)
    is AuthError.BadRequest -> message?.takeIf { it.isNotBlank() }
        ?: stringResource(id = R.string.login_error_bad_request)
    is AuthError.RateLimited -> stringResource(id = R.string.error_rate_limited)
    is AuthError.Server -> stringResource(id = R.string.login_error_server)
    is AuthError.Network -> stringResource(id = R.string.login_error_network)
    is AuthError.Unknown -> stringResource(id = R.string.login_error_unknown)
    is AuthError.DuplicateAccount -> message?.takeIf { it.isNotBlank() }
        ?: stringResource(id = R.string.register_error_duplicate_account)
}

fun isPasswordTooShort(input: String): Boolean =
    Validators.validatePassword(input) is FieldError.TooShort
