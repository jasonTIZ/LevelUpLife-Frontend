package com.example.leveluplife.domain.validation

sealed class FieldError {
    object Required : FieldError()
    object InvalidEmail : FieldError()
    data class TooShort(val min: Int) : FieldError()
    data class TooLong(val max: Int) : FieldError()
}

object Validators {

    const val EMAIL_MAX = 254
    const val PASSWORD_MIN = 6
    const val PASSWORD_MAX = 64

    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )

    fun validateEmail(input: String): FieldError? {
        val trimmed = input.trim()
        return when {
            trimmed.isEmpty() -> FieldError.Required
            trimmed.length > EMAIL_MAX -> FieldError.TooLong(EMAIL_MAX)
            !EMAIL_REGEX.matches(trimmed) -> FieldError.InvalidEmail
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
