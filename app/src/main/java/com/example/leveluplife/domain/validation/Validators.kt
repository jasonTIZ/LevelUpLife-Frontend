package com.example.leveluplife.domain.validation

sealed class FieldError {
    object Required : FieldError()
    object InvalidUserNameOrEmail : FieldError()
    object InvalidEmail : FieldError()
    object InvalidBirthdate : FieldError()
    object InvalidCharacters : FieldError()
    object PasswordMismatch : FieldError()
    object SameAsName : FieldError()
    data class TooShort(val min: Int) : FieldError()
    data class TooLong(val max: Int) : FieldError()
    data class InvalidRange(val min: Int, val max: Int) : FieldError()
}

object Validators {

    const val EMAIL_MAX = 254
    const val USERNAME_MIN = 3
    const val USERNAME_MAX = 32
    const val PASSWORD_MIN = 6
    const val PASSWORD_MAX = 64
    const val HABIT_TITLE_MIN = 5
    const val HABIT_TITLE_MAX = 100
    const val HABIT_DESCRIPTION_MAX = 500
    const val TASK_TITLE_MIN = 3
    const val TASK_TITLE_MAX = 100

    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )

    private val USERNAME_REGEX = Regex(
        "^[A-Za-z0-9](?:[A-Za-z0-9._-]*[A-Za-z0-9])?$"
    )

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
            input.any { it.isWhitespace() } -> FieldError.InvalidCharacters
            input.length < PASSWORD_MIN -> FieldError.TooShort(PASSWORD_MIN)
            input.length > PASSWORD_MAX -> FieldError.TooLong(PASSWORD_MAX)
            else -> null
        }
    }

    fun validateHabitTitle(input: String): FieldError? {
        return when {
            input.isEmpty() -> FieldError.Required
            input.length < HABIT_TITLE_MIN -> FieldError.TooShort(HABIT_TITLE_MIN)
            input.length > HABIT_TITLE_MAX -> FieldError.TooLong(HABIT_TITLE_MAX)
            else -> null
        }
    }

    fun validateHabitDescription(input: String): FieldError? {
        return when {
            input.length > HABIT_DESCRIPTION_MAX -> FieldError.TooLong(HABIT_DESCRIPTION_MAX)
            else -> null
        }
    }

    fun validateTaskTitle(input: String): FieldError? {
        return when {
            input.isEmpty() -> FieldError.Required
            input.length < TASK_TITLE_MIN -> FieldError.TooShort(TASK_TITLE_MIN)
            input.length > TASK_TITLE_MAX -> FieldError.TooLong(TASK_TITLE_MAX)
            else -> null
        }
    }

    fun validateTaskPeriodLength(input: Int): FieldError? {
        return when {
            input < 1 -> FieldError.Required
            else -> null
        }
    }

    fun validateTaskPeriodUnit(input: Int): FieldError? {
        return when {
            input < 1 -> FieldError.Required
            else -> null
        }
    }

    fun isValidEmail(input: String): Boolean = EMAIL_REGEX.matches(input.trim())
}
