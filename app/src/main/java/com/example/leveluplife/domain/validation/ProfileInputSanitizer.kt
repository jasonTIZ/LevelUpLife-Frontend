package com.example.leveluplife.domain.validation

object ProfileInputSanitizer {

    fun sanitizePersonName(input: String): String =
        input
            .replace(Regex("[\\p{Cc}\\p{Cf}]"), "")
            .replace(Regex("\\s+"), " ")
            .take(ProfileValidators.NAME_MAX)

    fun sanitizeEmail(input: String): String =
        input
            .replace(Regex("[\\p{Cc}\\p{Cf}\\s]"), "")
            .take(Validators.EMAIL_MAX)

    fun sanitizeUserName(input: String): String =
        input
            .replace(Regex("\\s"), "")
            .filter { it.isLetterOrDigit() || it == '.' || it == '_' || it == '-' }
            .take(ProfileValidators.USERNAME_MAX)

    fun sanitizeBirthdate(input: String): String =
        input
            .filter { it.isDigit() || it == '-' }
            .take(ProfileValidators.BIRTHDATE_MAX_LENGTH)

    fun sanitizeBio(input: String): String =
        input
            .replace(Regex("[\\p{Cc}]"), "")
            .take(ProfileValidators.BIO_MAX)

    fun sanitizeProfile(state: ProfileFormValues): ProfileFormValues = ProfileFormValues(
        name = sanitizePersonName(state.name).trim(),
        lastName = sanitizePersonName(state.lastName).trim(),
        email = sanitizeEmail(state.email).trim(),
        userName = sanitizeUserName(state.userName).trim(),
        birthdate = sanitizeBirthdate(state.birthdate).trim(),
        bio = sanitizeBio(state.bio).trim(),
    )
}

data class ProfileFormValues(
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    val userName: String = "",
    val birthdate: String = "",
    val bio: String = "",
)
