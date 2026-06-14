package com.example.leveluplife.domain.validation

import java.util.Calendar
import java.util.GregorianCalendar

sealed class AvatarValidationError {
    data object UnsupportedType : AvatarValidationError()
    data object TooLarge : AvatarValidationError()
}

object AvatarValidator {
    const val MAX_BYTES = 2 * 1024 * 1024 // 2 MB

    private val ALLOWED_MIME_TYPES = setOf(
        "image/jpeg",
        "image/png",
        "image/webp",
    )

    fun validate(mimeType: String?, sizeBytes: Long): AvatarValidationError? {
        val normalizedMime = mimeType?.lowercase().orEmpty()
        if (normalizedMime.isBlank() || normalizedMime !in ALLOWED_MIME_TYPES) {
            return AvatarValidationError.UnsupportedType
        }
        if (sizeBytes <= 0L || sizeBytes > MAX_BYTES) {
            return AvatarValidationError.TooLarge
        }
        return null
    }
}

object ProfileValidators {
    const val NAME_MIN = 2
    const val NAME_MAX = 50
    const val USERNAME_MIN = 3
    const val USERNAME_MAX = 20
    const val BIO_MAX = 200
    const val BIRTHDATE_MAX_LENGTH = 10
    const val BIRTHDATE_MIN_YEAR = 1900

    private val BIRTHDATE_REGEX = Regex("^\\d{4}-\\d{2}-\\d{2}$")
    private val PERSON_NAME_REGEX = Regex("^[\\p{L}](?:[\\p{L} '\\-]*[\\p{L}])?$")
    private val USERNAME_REGEX = Regex("^[A-Za-z0-9](?:[A-Za-z0-9._-]*[A-Za-z0-9])?$")

    fun validateName(input: String): FieldError? =
        validatePersonName(input, NAME_MIN, NAME_MAX)

    fun validateLastName(input: String): FieldError? =
        validatePersonName(input, NAME_MIN, NAME_MAX)

    fun validateEmail(input: String): FieldError? {
        val sanitized = ProfileInputSanitizer.sanitizeEmail(input).trim()
        return when {
            sanitized.isEmpty() -> FieldError.Required
            sanitized.length > Validators.EMAIL_MAX -> FieldError.TooLong(Validators.EMAIL_MAX)
            !Validators.isValidEmail(sanitized) -> FieldError.InvalidEmail
            else -> null
        }
    }

    fun validateUserName(input: String): FieldError? {
        val sanitized = ProfileInputSanitizer.sanitizeUserName(input).trim()
        return when {
            sanitized.isEmpty() -> FieldError.Required
            sanitized.length < USERNAME_MIN -> FieldError.TooShort(USERNAME_MIN)
            sanitized.length > USERNAME_MAX -> FieldError.TooLong(USERNAME_MAX)
            !USERNAME_REGEX.matches(sanitized) -> FieldError.InvalidUserNameOrEmail
            else -> null
        }
    }

    fun validateBirthdate(input: String): FieldError? {
        val sanitized = ProfileInputSanitizer.sanitizeBirthdate(input).trim()
        if (sanitized.isEmpty()) return null
        if (!BIRTHDATE_REGEX.matches(sanitized)) return FieldError.InvalidBirthdate

        val parts = sanitized.split("-")
        if (parts.size != 3) return FieldError.InvalidBirthdate

        val year = parts[0].toIntOrNull() ?: return FieldError.InvalidBirthdate
        val month = parts[1].toIntOrNull() ?: return FieldError.InvalidBirthdate
        val day = parts[2].toIntOrNull() ?: return FieldError.InvalidBirthdate

        if (year < BIRTHDATE_MIN_YEAR) return FieldError.InvalidBirthdate
        if (!isRealCalendarDate(year, month, day)) return FieldError.InvalidBirthdate
        if (isFutureDate(year, month, day)) return FieldError.InvalidBirthdate

        return null
    }

    fun validateBio(input: String): FieldError? {
        val sanitized = ProfileInputSanitizer.sanitizeBio(input)
        return if (sanitized.length > BIO_MAX) FieldError.TooLong(BIO_MAX) else null
    }

    fun validateAll(values: ProfileFormValues): Map<ProfileFieldKey, FieldError> {
        val sanitized = ProfileInputSanitizer.sanitizeProfile(values)
        return buildMap {
            validateName(sanitized.name)?.let { put(ProfileFieldKey.NAME, it) }
            validateLastName(sanitized.lastName)?.let { put(ProfileFieldKey.LAST_NAME, it) }
            validateEmail(sanitized.email)?.let { put(ProfileFieldKey.EMAIL, it) }
            validateUserName(sanitized.userName)?.let { put(ProfileFieldKey.USER_NAME, it) }
            validateBirthdate(sanitized.birthdate)?.let { put(ProfileFieldKey.BIRTHDATE, it) }
            validateBio(sanitized.bio)?.let { put(ProfileFieldKey.BIO, it) }
        }
    }

    private fun validatePersonName(input: String, min: Int, max: Int): FieldError? {
        val sanitized = ProfileInputSanitizer.sanitizePersonName(input).trim()
        return when {
            sanitized.isEmpty() -> FieldError.Required
            sanitized.length < min -> FieldError.TooShort(min)
            sanitized.length > max -> FieldError.TooLong(max)
            !PERSON_NAME_REGEX.matches(sanitized) -> FieldError.InvalidCharacters
            else -> null
        }
    }

    private fun isRealCalendarDate(year: Int, month: Int, day: Int): Boolean {
        if (month !in 1..12 || day < 1) return false
        val calendar = GregorianCalendar(year, month - 1, day)
        calendar.isLenient = false
        return try {
            calendar.time
            calendar.get(Calendar.YEAR) == year &&
                calendar.get(Calendar.MONTH) == month - 1 &&
                calendar.get(Calendar.DAY_OF_MONTH) == day
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    private fun isFutureDate(year: Int, month: Int, day: Int): Boolean {
        val today = Calendar.getInstance()
        val candidate = GregorianCalendar(year, month - 1, day)
        return candidate.after(today)
    }
}

enum class ProfileFieldKey {
    NAME,
    LAST_NAME,
    EMAIL,
    USER_NAME,
    BIRTHDATE,
    BIO,
}
