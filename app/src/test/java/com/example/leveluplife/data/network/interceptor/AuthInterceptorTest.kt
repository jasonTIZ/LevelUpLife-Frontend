package com.example.leveluplife.data.network.interceptor

import com.example.leveluplife.data.auth.FakeTokenStore
import com.example.leveluplife.data.auth.SessionEvents
import com.example.leveluplife.data.network.ApiRoutes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthInterceptorTest {

    private val mockServer = MockWebServer()
    private val store = FakeTokenStore()
    private val sessionEvents = SessionEvents()

    @After
    fun tearDown() = mockServer.shutdown()

    private fun client() = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(store, sessionEvents))
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
    fun `respuesta 401 en endpoint autenticado limpia el token y emite sesión expirada`() = runTest {
        store.saveTokens("expired-token", null)
        mockServer.enqueue(MockResponse().setResponseCode(401))
        val events = mutableListOf<Unit>()
        val job = launch { sessionEvents.expired.collect { events.add(Unit) } }

        client().newCall(Request.Builder().url(mockServer.url("/api/resource")).build())
            .execute().close()

        testScheduler.advanceUntilIdle()
        job.cancel()

        assertNull(store.accessToken())
        assertEquals(1, store.clearCallCount)
        assertEquals(1, events.size)
    }

    @Test
    fun `respuesta 401 en login no emite sesión expirada`() = runTest {
        store.saveTokens("expired-token", null)
        mockServer.enqueue(MockResponse().setResponseCode(401))
        val events = mutableListOf<Unit>()
        val job = launch { sessionEvents.expired.collect { events.add(Unit) } }

        client().newCall(Request.Builder().url(mockServer.url("/api/auth/login")).build())
            .execute().close()

        testScheduler.advanceUntilIdle()
        job.cancel()

        assertTrue(events.isEmpty())
    }

    @Test
    fun `respuesta 401 en login con path PascalCase no emite sesión expirada`() = runTest {
        store.saveTokens("expired-token", null)
        mockServer.enqueue(MockResponse().setResponseCode(401))
        val events = mutableListOf<Unit>()
        val job = launch { sessionEvents.expired.collect { events.add(Unit) } }

        client().newCall(Request.Builder().url(mockServer.url("/api/Auth/Login")).build())
            .execute().close()

        testScheduler.advanceUntilIdle()
        job.cancel()

        assertTrue(events.isEmpty())
    }

    @Test
    fun `ApiRoutes reconoce login sin importar mayúsculas`() {
        assertTrue(ApiRoutes.isLoginRequest("/api/auth/login"))
        assertTrue(ApiRoutes.isLoginRequest("/api/Auth/Login"))
        assertFalse(ApiRoutes.isLoginRequest("/api/Player/profile"))
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
