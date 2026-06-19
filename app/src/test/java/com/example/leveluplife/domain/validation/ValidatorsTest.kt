package com.example.leveluplife.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    // -------- Email / Username --------

    @Test
    fun `identifier vacio devuelve Required`() {
        assertEquals(FieldError.Required, Validators.validateUserNameOrEmail(""))
    }

    @Test
    fun `identifier solo espacios devuelve Required`() {
        assertEquals(FieldError.Required, Validators.validateUserNameOrEmail("   "))
    }

    @Test
    fun `email sin dominio es invalido`() {
        assertEquals(
            FieldError.InvalidUserNameOrEmail,
            Validators.validateUserNameOrEmail("user@"),
        )
    }

    @Test
    fun `email sin TLD es invalido`() {
        assertEquals(
            FieldError.InvalidUserNameOrEmail,
            Validators.validateUserNameOrEmail("user@localhost"),
        )
    }

    @Test
    fun `email con espacios internos es invalido`() {
        assertEquals(
            FieldError.InvalidUserNameOrEmail,
            Validators.validateUserNameOrEmail("user name@correo.com"),
        )
    }

    @Test
    fun `email valido devuelve null`() {
        assertNull(Validators.validateUserNameOrEmail("darwin@levelup.life"))
    }

    @Test
    fun `email valido con plus y subdominio devuelve null`() {
        assertNull(Validators.validateUserNameOrEmail("darwin+tag@dev.levelup.life"))
    }

    @Test
    fun `email mayor a 254 caracteres devuelve TooLong`() {
        val local = "a".repeat(250)
        val email = "$local@x.io"
        val result = Validators.validateUserNameOrEmail(email)
        assertTrue(result is FieldError.TooLong)
    }

    @Test
    fun `username valido devuelve null`() {
        assertNull(Validators.validateUserNameOrEmail("darwin"))
    }

    @Test
    fun `username con punto guion bajo y guion devuelve null`() {
        assertNull(Validators.validateUserNameOrEmail("darwin.dev_98-x"))
    }

    @Test
    fun `username menor al minimo devuelve TooShort`() {
        val result = Validators.validateUserNameOrEmail("ab")
        assertTrue(result is FieldError.TooShort)
        assertEquals(Validators.USERNAME_MIN, (result as FieldError.TooShort).min)
    }

    @Test
    fun `username mayor al maximo devuelve TooLong`() {
        val result = Validators.validateUserNameOrEmail("a".repeat(Validators.USERNAME_MAX + 1))
        assertTrue(result is FieldError.TooLong)
    }

    @Test
    fun `username con espacios es invalido`() {
        assertEquals(
            FieldError.InvalidUserNameOrEmail,
            Validators.validateUserNameOrEmail("darwin 98"),
        )
    }

    @Test
    fun `username con caracteres especiales es invalido`() {
        assertEquals(
            FieldError.InvalidUserNameOrEmail,
            Validators.validateUserNameOrEmail("darwin#98"),
        )
    }

    @Test
    fun `username que empieza con punto es invalido`() {
        assertEquals(
            FieldError.InvalidUserNameOrEmail,
            Validators.validateUserNameOrEmail(".darwin"),
        )
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
    fun `password con espacios devuelve InvalidCharacters`() {
        assertEquals(FieldError.InvalidCharacters, Validators.validatePassword("123 456"))
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
