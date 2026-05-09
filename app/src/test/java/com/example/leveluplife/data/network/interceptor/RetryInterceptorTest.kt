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
}
