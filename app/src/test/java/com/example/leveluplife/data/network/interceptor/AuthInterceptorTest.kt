package com.example.leveluplife.data.network.interceptor

import com.example.leveluplife.data.auth.FakeTokenStore
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthInterceptorTest {

    private val mockServer = MockWebServer()
    private val store = FakeTokenStore()

    @After
    fun tearDown() = mockServer.shutdown()

    private fun client() = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(store))
        .build()

    // Scenario: Authorization header attached
    // Given user logged in
    // When client sends request
    // Then Authorization: Bearer <token> is included
    @Test
    fun `petición autenticada incluye header Authorization Bearer con el token activo`() {
        store.saveTokens("my-access-token", null)
        mockServer.enqueue(MockResponse().setResponseCode(200))

        client().newCall(Request.Builder().url(mockServer.url("/api/resource")).build())
            .execute().close()

        assertEquals("Bearer my-access-token", mockServer.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `petición sin sesión no adjunta header Authorization`() {
        mockServer.enqueue(MockResponse().setResponseCode(200))

        client().newCall(Request.Builder().url(mockServer.url("/api/resource")).build())
            .execute().close()

        assertNull(mockServer.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `respuesta 401 limpia el token del almacenamiento`() {
        store.saveTokens("expired-token", null)
        mockServer.enqueue(MockResponse().setResponseCode(401))

        client().newCall(Request.Builder().url(mockServer.url("/api/resource")).build())
            .execute().close()

        assertNull(store.accessToken())
        assertEquals(1, store.clearCallCount)
    }

    @Test
    fun `respuesta 200 no altera el almacenamiento de tokens`() {
        store.saveTokens("valid-token", null)
        mockServer.enqueue(MockResponse().setResponseCode(200))

        client().newCall(Request.Builder().url(mockServer.url("/api/resource")).build())
            .execute().close()

        assertEquals("valid-token", store.accessToken())
        assertEquals(0, store.clearCallCount)
    }
}
