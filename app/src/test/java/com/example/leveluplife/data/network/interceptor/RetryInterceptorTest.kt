package com.example.leveluplife.data.network.interceptor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class RetryInterceptorTest {

    private val mockServer = MockWebServer()

    @After
    fun tearDown() = mockServer.shutdown()

    private fun client(maxAttempts: Int = 3) = OkHttpClient.Builder()
        .addInterceptor(RetryInterceptor(maxAttempts))
        .build()

    private fun get() = Request.Builder().url(mockServer.url("/")).build()

    // Scenario: Retry transient errors
    // Given a transient network failure
    // When request fails
    // Then client retries configured attempts before surfacing an error
    @Test
    fun `reintenta errores 5xx y devuelve la respuesta exitosa`() {
        mockServer.enqueue(MockResponse().setResponseCode(503))
        mockServer.enqueue(MockResponse().setResponseCode(503))
        mockServer.enqueue(MockResponse().setResponseCode(200))

        val response = client(maxAttempts = 3).newCall(get()).execute()

        assertEquals(200, response.code)
        assertEquals(3, mockServer.requestCount)
        response.close()
    }

    @Test
    fun `devuelve el último 5xx al agotar los intentos configurados`() {
        repeat(3) { mockServer.enqueue(MockResponse().setResponseCode(503)) }

        val response = client(maxAttempts = 3).newCall(get()).execute()

        assertEquals(503, response.code)
        assertEquals(3, mockServer.requestCount)
        response.close()
    }

    @Test
    fun `reintenta IOException de red y devuelve la respuesta exitosa`() {
        mockServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        mockServer.enqueue(MockResponse().setResponseCode(200))

        val response = client(maxAttempts = 3).newCall(get()).execute()

        assertEquals(200, response.code)
        response.close()
    }

    @Test
    fun `lanza IOException al agotar todos los intentos por fallo de red`() {
        repeat(3) { mockServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)) }

        assertThrows(IOException::class.java) {
            client(maxAttempts = 3).newCall(get()).execute()
        }
    }

    @Test
    fun `no reintenta errores 4xx de cliente`() {
        mockServer.enqueue(MockResponse().setResponseCode(400))

        val response = client(maxAttempts = 3).newCall(get()).execute()

        assertEquals(400, response.code)
        assertEquals(1, mockServer.requestCount)
        response.close()
    }

    @Test
    fun `no reintenta peticiones exitosas`() {
        mockServer.enqueue(MockResponse().setResponseCode(200))

        val response = client(maxAttempts = 3).newCall(get()).execute()

        assertEquals(200, response.code)
        assertEquals(1, mockServer.requestCount)
        response.close()
    }

    // --- 429 Rate Limiting ---

    // Scenario: 429 with Retry-After 0 retries immediately and succeeds
    @Test
    fun `reintenta 429 con Retry-After cero y devuelve la respuesta exitosa`() {
        mockServer.enqueue(MockResponse().setResponseCode(429).addHeader("Retry-After", "0"))
        mockServer.enqueue(MockResponse().setResponseCode(429).addHeader("Retry-After", "0"))
        mockServer.enqueue(MockResponse().setResponseCode(200))

        val response = client(maxAttempts = 3).newCall(get()).execute()

        assertEquals(200, response.code)
        assertEquals(3, mockServer.requestCount)
        response.close()
    }

    @Test
    fun `devuelve el último 429 al agotar los intentos configurados`() {
        repeat(3) { mockServer.enqueue(MockResponse().setResponseCode(429).addHeader("Retry-After", "0")) }

        val response = client(maxAttempts = 3).newCall(get()).execute()

        assertEquals(429, response.code)
        assertEquals(3, mockServer.requestCount)
        response.close()
    }

    @Test
    fun `429 sin Retry-After usa backoff exponencial con override de cero para el test`() {
        val noBackoff: (Int, String?, Int) -> Long = { _, _, _ -> 0L }
        val client = OkHttpClient.Builder()
            .addInterceptor(RetryInterceptor(maxAttempts = 3, backoffMs = noBackoff))
            .build()
        mockServer.enqueue(MockResponse().setResponseCode(429))
        mockServer.enqueue(MockResponse().setResponseCode(200))

        val response = client.newCall(get()).execute()

        assertEquals(200, response.code)
        assertEquals(2, mockServer.requestCount)
        response.close()
    }

    // --- BackoffStrategy unit tests (no HTTP needed) ---

    @Test
    fun `backoff para 429 sin header usa exponencial con base 1s`() {
        val backoff = RetryInterceptor.computeDefaultBackoff
        assertEquals(1_000L, backoff(429, null, 1))
        assertEquals(2_000L, backoff(429, null, 2))
        assertEquals(4_000L, backoff(429, null, 3))
    }

    @Test
    fun `backoff para 429 respeta el header Retry-After en segundos`() {
        val backoff = RetryInterceptor.computeDefaultBackoff
        assertEquals(5_000L, backoff(429, "5", 1))
        assertEquals(0L, backoff(429, "0", 1))
    }

    @Test
    fun `backoff para 5xx es siempre cero`() {
        val backoff = RetryInterceptor.computeDefaultBackoff
        assertEquals(0L, backoff(500, null, 1))
        assertEquals(0L, backoff(503, null, 2))
        assertEquals(0L, backoff(502, "5", 1))
    }

    @Test
    fun `backoff para 429 esta limitado al maximo de 30 segundos`() {
        val backoff = RetryInterceptor.computeDefaultBackoff
        // attempt 16 → 2^15 * 1000 = 32_768_000ms, capped at 30_000
        assertEquals(30_000L, backoff(429, null, 16))
    }
}
