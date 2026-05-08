package com.example.leveluplife.domain.validation

sealed class FieldError {
    object Required : FieldError()
    object InvalidUserNameOrEmail : FieldError()
    data class TooShort(val min: Int) : FieldError()
    data class TooLong(val max: Int) : FieldError()
}

object Validators {

    const val EMAIL_MAX = 254
    const val USERNAME_MIN = 3
    const val USERNAME_MAX = 32
    const val PASSWORD_MIN = 6
    const val PASSWORD_MAX = 64

    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )

    private val USERNAME_REGEX = Regex(
        "^[A-Za-z0-9](?:[A-Za-z0-9._-]*[A-Za-z0-9])?$"
    )

    /**
     * Acepta:
     *  - Email con formato `local@dominio.tld`.
     *  - Username de 3 a 32 caracteres alfanuméricos, con `.`, `_` o `-` permitidos
     *    en el medio (no al inicio/fin).
     */
    fun validateUserNameOrEmail(input: String): FieldError? {
        val trimmed = input.trim()
        return when {
            trimmed.isEmpty() -> FieldError.Required
            trimmed.length > EMAIL_MAX -> FieldError.TooLong(EMAIL_MAX)
            EMAIL_REGEX.matches(trimmed) -> null
            trimmed.contains("@") -> FieldError.InvalidUserNameOrEmail
            trimmed.length < USERNAME_MIN -> FieldError.TooShort(USERNAME_MIN)
            trimmed.length > USERNAME_MAX -> FieldError.TooLong(USERNAME_MAX)
            !USERNAME_REGEX.matches(trimmed) -> FieldError.InvalidUserNameOrEmail
            else -> null
        }
    }

    fun validatePassword(input: String): FieldError? {
        return when {
            input.isEmpty() -> FieldError.Required
            input.length < PASSWORD_MIN -> FieldError.TooShort(PASSWORD_MIN)
            input.length > PASSWORD_MAX -> FieldError.TooLong(PASSWORD_MAX)
            else -> null
        }
    }
}
