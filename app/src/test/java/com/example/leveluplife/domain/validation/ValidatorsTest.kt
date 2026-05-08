package com.example.leveluplife.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    // -------- Email --------

    @Test
    fun `email vacio devuelve Required`() {
        assertEquals(FieldError.Required, Validators.validateEmail(""))
    }

    @Test
    fun `email solo espacios devuelve Required`() {
        assertEquals(FieldError.Required, Validators.validateEmail("   "))
    }

    @Test
    fun `email sin arroba es invalido`() {
        assertEquals(FieldError.InvalidEmail, Validators.validateEmail("usuario.com"))
    }

    @Test
    fun `email sin dominio es invalido`() {
        assertEquals(FieldError.InvalidEmail, Validators.validateEmail("user@"))
    }

    @Test
    fun `email sin TLD es invalido`() {
        assertEquals(FieldError.InvalidEmail, Validators.validateEmail("user@localhost"))
    }

    @Test
    fun `email con espacios internos es invalido`() {
        assertEquals(FieldError.InvalidEmail, Validators.validateEmail("user name@correo.com"))
    }

    @Test
    fun `email valido devuelve null`() {
        assertNull(Validators.validateEmail("darwin@levelup.life"))
    }

    @Test
    fun `email valido con plus y subdominio devuelve null`() {
        assertNull(Validators.validateEmail("darwin+tag@dev.levelup.life"))
    }

    @Test
    fun `email mayor a 254 caracteres devuelve TooLong`() {
        val local = "a".repeat(250)
        val email = "$local@x.io"
        val result = Validators.validateEmail(email)
        assertTrue(result is FieldError.TooLong)
    }

    // -------- Password --------

    @Test
    fun `password vacia devuelve Required`() {
        assertEquals(FieldError.Required, Validators.validatePassword(""))
    }

    @Test
    fun `password menor al minimo devuelve TooShort`() {
        val result = Validators.validatePassword("12345")
        assertTrue(result is FieldError.TooShort)
        assertEquals(Validators.PASSWORD_MIN, (result as FieldError.TooShort).min)
    }

    @Test
    fun `password con minimo exacto es valida`() {
        assertNull(Validators.validatePassword("123456"))
    }

    @Test
    fun `password en limite maximo es valida`() {
        assertNull(Validators.validatePassword("a".repeat(Validators.PASSWORD_MAX)))
    }

    @Test
    fun `password mayor al maximo devuelve TooLong`() {
        val result = Validators.validatePassword("a".repeat(Validators.PASSWORD_MAX + 1))
        assertTrue(result is FieldError.TooLong)
    }
}
