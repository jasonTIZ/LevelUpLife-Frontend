package com.example.leveluplife.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AvatarValidatorTest {

    @Test
    fun `valid jpeg within size passes`() {
        assertNull(AvatarValidator.validate("image/jpeg", 1024))
    }

    @Test
    fun `file too large fails`() {
        assertEquals(
            AvatarValidationError.TooLarge,
            AvatarValidator.validate("image/png", AvatarValidator.MAX_BYTES + 1L),
        )
    }

    @Test
    fun `unsupported mime fails`() {
        assertEquals(
            AvatarValidationError.UnsupportedType,
            AvatarValidator.validate("image/gif", 1024),
        )
    }
}

class ProfileInputSanitizerTest {

    @Test
    fun `sanitizePersonName collapses spaces and strips control chars`() {
        val result = ProfileInputSanitizer.sanitizePersonName("  Aaron\u0001  Test  ")
        assertEquals(" Aaron Test ", result)
    }

    @Test
    fun `sanitizeEmail removes spaces`() {
        assertEquals("aaron@test.com", ProfileInputSanitizer.sanitizeEmail(" aaron @test.com "))
    }

    @Test
    fun `sanitizeUserName removes invalid chars`() {
        assertEquals("aaron.dev_1", ProfileInputSanitizer.sanitizeUserName(" aaron dev!@#._1 "))
    }

    @Test
    fun `sanitizeBirthdate keeps digits and hyphens only`() {
        assertEquals("2000-01-15", ProfileInputSanitizer.sanitizeBirthdate("20abc00/01-15xyz"))
    }

    @Test
    fun `sanitizeBio caps length`() {
        val longBio = "a".repeat(ProfileValidators.BIO_MAX + 10)
        assertEquals(ProfileValidators.BIO_MAX, ProfileInputSanitizer.sanitizeBio(longBio).length)
    }
}

class ProfileValidatorsTest {

    @Test
    fun `validateName rejects empty`() {
        assertTrue(ProfileValidators.validateName("") is FieldError.Required)
    }

    @Test
    fun `validateName rejects digits`() {
        assertTrue(ProfileValidators.validateName("Aaron1") is FieldError.InvalidCharacters)
    }

    @Test
    fun `validateName accepts accented letters`() {
        assertNull(ProfileValidators.validateName("María"))
    }

    @Test
    fun `validateEmail rejects invalid format`() {
        assertTrue(ProfileValidators.validateEmail("not-an-email") is FieldError.InvalidEmail)
    }

    @Test
    fun `validateEmail accepts valid email`() {
        assertNull(ProfileValidators.validateEmail("aaron@test.com"))
    }

    @Test
    fun `validateUserName rejects too short`() {
        assertTrue(ProfileValidators.validateUserName("ab") is FieldError.TooShort)
    }

    @Test
    fun `validateUserName rejects leading dot`() {
        assertTrue(ProfileValidators.validateUserName(".aarontest") is FieldError.InvalidUserNameOrEmail)
    }

    @Test
    fun `validateBirthdate accepts valid date`() {
        assertNull(ProfileValidators.validateBirthdate("2000-01-15"))
    }

    @Test
    fun `validateBirthdate rejects invalid format`() {
        assertTrue(ProfileValidators.validateBirthdate("15-01-2000") is FieldError.InvalidBirthdate)
    }

    @Test
    fun `validateBirthdate rejects impossible date`() {
        assertTrue(ProfileValidators.validateBirthdate("2000-02-31") is FieldError.InvalidBirthdate)
    }

    @Test
    fun `validateBirthdate rejects future date`() {
        assertTrue(ProfileValidators.validateBirthdate("2099-12-31") is FieldError.InvalidBirthdate)
    }

    @Test
    fun `validateBio rejects too long`() {
        val longBio = "a".repeat(ProfileValidators.BIO_MAX + 1)
        assertTrue(ProfileValidators.validateBio(longBio) is FieldError.TooLong)
    }

    @Test
    fun `validateAll returns multiple errors`() {
        val errors = ProfileValidators.validateAll(
            ProfileFormValues(
                name = "",
                email = "bad",
                userName = "x",
            ),
        )
        assertTrue(errors.containsKey(ProfileFieldKey.NAME))
        assertTrue(errors.containsKey(ProfileFieldKey.EMAIL))
        assertTrue(errors.containsKey(ProfileFieldKey.USER_NAME))
    }
}
