package com.example.leveluplife.data.error

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AuthErrorMapperTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `400 mapea a BadRequest`() {
        val r = AuthErrorMapper.fromHttpCode(400, """{"message":"campo invalido"}""", json)
        assertTrue(r is AuthError.BadRequest)
        assertEquals("campo invalido", (r as AuthError.BadRequest).message)
    }

    @Test
    fun `401 mapea a InvalidCredentials`() {
        val r = AuthErrorMapper.fromHttpCode(401, """{"message":"Invalid credentials"}""", json)
        assertTrue(r is AuthError.InvalidCredentials)
    }

    @Test
    fun `423 mapea a AccountLocked`() {
        val r = AuthErrorMapper.fromHttpCode(423, null, json)
        assertTrue(r is AuthError.AccountLocked)
    }

    @Test
    fun `5xx mapea a Server`() {
        val r500 = AuthErrorMapper.fromHttpCode(500, null, json)
        val r503 = AuthErrorMapper.fromHttpCode(503, null, json)
        assertTrue(r500 is AuthError.Server)
        assertTrue(r503 is AuthError.Server)
    }

    @Test
    fun `IOException mapea a Network`() {
        val r = AuthErrorMapper.fromThrowable(IOException("offline"))
        assertTrue(r is AuthError.Network)
    }

    @Test
    fun `SocketTimeoutException mapea a Network timeout`() {
        val r = AuthErrorMapper.fromThrowable(SocketTimeoutException())
        assertTrue(r is AuthError.Network)
        assertEquals("timeout", (r as AuthError.Network).message)
    }

    @Test
    fun `UnknownHostException mapea a Network unknown_host`() {
        val r = AuthErrorMapper.fromThrowable(UnknownHostException("nope"))
        assertTrue(r is AuthError.Network)
        assertEquals("unknown_host", (r as AuthError.Network).message)
    }

    @Test
    fun `cuerpo JSON sin message usa errores anidados`() {
        val body = """{"errors":{"email":["formato invalido"]}}"""
        val r = AuthErrorMapper.fromHttpCode(400, body, json)
        assertTrue(r is AuthError.BadRequest)
        assertEquals("formato invalido", (r as AuthError.BadRequest).message)
    }

    @Test
    fun `cuerpo no JSON se devuelve recortado`() {
        val r = AuthErrorMapper.fromHttpCode(401, "Texto crudo", json)
        assertTrue(r is AuthError.InvalidCredentials)
        assertEquals("Texto crudo", (r as AuthError.InvalidCredentials).message)
    }
}
