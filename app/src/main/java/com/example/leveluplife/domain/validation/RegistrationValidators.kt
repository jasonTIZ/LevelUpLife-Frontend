package com.example.leveluplife.domain.validation

import com.example.leveluplife.domain.validation.ProfileInputSanitizer.sanitizeBirthdate
import com.example.leveluplife.domain.validation.ProfileInputSanitizer.sanitizeEmail
import com.example.leveluplife.domain.validation.ProfileInputSanitizer.sanitizePersonName
import com.example.leveluplife.domain.validation.ProfileInputSanitizer.sanitizeUserName

object RegistrationValidators {
    const val NAME_MIN = 1
    const val NAME_MAX = 50
    const val EMAIL_MAX = 150
    const val USERNAME_MIN = 3
    const val USERNAME_MAX = 50
    const val PASSWORD_MIN = 6
    const val PASSWORD_MAX = 100

    private val USERNAME_REGEX = Regex("^[A-Za-z0-9._]+$")

    fun validateName(input: String): FieldError? {
        val sanitized = sanitizePersonName(input).trim()
        return when {
            sanitized.isEmpty() -> FieldError.Required
            sanitized.length > NAME_MAX -> FieldError.TooLong(NAME_MAX)
            else -> null
        }
    }

    fun validateLastName(input: String): FieldError? {
        val sanitized = sanitizePersonName(input).trim()
        return when {
            sanitized.isEmpty() -> FieldError.Required
            sanitized.length > NAME_MAX -> FieldError.TooLong(NAME_MAX)
            else -> null
        }
    }

    fun validateEmail(input: String): FieldError? {
        val sanitized = sanitizeEmail(input).trim()
        return when {
            sanitized.isEmpty() -> FieldError.Required
            sanitized.length > EMAIL_MAX -> FieldError.TooLong(EMAIL_MAX)
            !Validators.isValidEmail(sanitized) -> FieldError.InvalidEmail
            else -> null
        }
    }

    fun validateBirthdate(input: String): FieldError? {
        val sanitized = sanitizeBirthdate(input).trim()
        if (sanitized.isEmpty()) return FieldError.Required
        return ProfileValidators.validateBirthdate(sanitized)
    }

    fun validateUserName(input: String, firstName: String): FieldError? {
        val sanitized = sanitizeUserName(input).trim()
        return when {
            sanitized.isEmpty() -> FieldError.Required
            sanitized.length < USERNAME_MIN -> FieldError.TooShort(USERNAME_MIN)
            sanitized.length > USERNAME_MAX -> FieldError.TooLong(USERNAME_MAX)
            !USERNAME_REGEX.matches(sanitized) -> FieldError.InvalidUserNameOrEmail
            sanitized.equals(firstName.trim(), ignoreCase = true) -> FieldError.SameAsName
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

    fun validateConfirmPassword(password: String, confirmPassword: String): FieldError? {
        return when {
            confirmPassword.isEmpty() -> FieldError.Required
            confirmPassword != password -> FieldError.PasswordMismatch
            else -> null
        }
    }

    fun validateClassId(classId: Int?): FieldError? =
        if (classId == null || classId <= 0) FieldError.Required else null
}

enum class RegisterFieldKey {
    NAME,
    LAST_NAME,
    EMAIL,
    BIRTHDATE,
    USER_NAME,
    PASSWORD,
    CLASS_ID,
}
