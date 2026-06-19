package com.example.leveluplife.data.network.interceptor

import com.example.leveluplife.data.auth.FakeTokenStore
import com.example.leveluplife.data.auth.SessionEvent
import com.example.leveluplife.data.auth.SessionEvents
import com.example.leveluplife.data.network.ApiRoutes
import com.example.leveluplife.ui.profile.FakeProfileCache
import com.example.leveluplife.ui.profile.sampleProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthInterceptorTest {

    private val mockServer = MockWebServer()
    private val store = FakeTokenStore()
    private val sessionEvents = SessionEvents()
    private val profileCache = FakeProfileCache(initial = sampleProfile())

    @Before
    fun setUp() = mockServer.start()

    @After
    fun tearDown() = mockServer.shutdown()

    private fun client() = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(store, sessionEvents, profileCache))
        .build()

    @Test
    fun `authenticated request includes Authorization Bearer header with active token`() {
        store.saveTokens("my-access-token", null)
        mockServer.enqueue(MockResponse().setResponseCode(200))

        client().newCall(Request.Builder().url(mockServer.url("/api/resource")).build())
            .execute().close()

        assertEquals("Bearer my-access-token", mockServer.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `request without session does not attach Authorization header`() {
        mockServer.enqueue(MockResponse().setResponseCode(200))

        client().newCall(Request.Builder().url(mockServer.url("/api/resource")).build())
            .execute().close()

        assertNull(mockServer.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `401 on authenticated endpoint clears token profile memory and emits SESSION_EXPIRED`() {
        store.saveTokens("expired-token", null)
        mockServer.enqueue(MockResponse().setResponseCode(401))

        client().newCall(Request.Builder().url(mockServer.url("/api/resource")).build())
            .execute().close()

        assertNull(store.accessToken())
        assertNull(profileCache.profile.value)
        assertEquals(1, store.clearCallCount)
        assertEquals(1, sessionEvents.recordedEvents().count { it == SessionEvent.SESSION_EXPIRED })
    }

    @Test
    fun `401 on login does not emit SESSION_EXPIRED`() = runTest {
        store.saveTokens("expired-token", null)
        mockServer.enqueue(MockResponse().setResponseCode(401))

        client().newCall(Request.Builder().url(mockServer.url("/api/auth/login")).build())
            .execute().close()

        assertTrue(sessionEvents.recordedEvents().isEmpty())
    }

    @Test
    fun `401 on login with PascalCase path does not emit SESSION_EXPIRED`() = runTest {
        store.saveTokens("expired-token", null)
        mockServer.enqueue(MockResponse().setResponseCode(401))

        client().newCall(Request.Builder().url(mockServer.url("/api/Auth/Login")).build())
            .execute().close()

        assertTrue(sessionEvents.recordedEvents().isEmpty())
    }

    @Test
    fun `ApiRoutes recognizes login regardless of casing`() {
        assertTrue(ApiRoutes.isLoginRequest("/api/auth/login"))
        assertTrue(ApiRoutes.isLoginRequest("/api/Auth/Login"))
        assertFalse(ApiRoutes.isLoginRequest("/api/Player/profile"))
    }

    @Test
    fun `200 response does not alter token storage`() {
        store.saveTokens("valid-token", null)
        mockServer.enqueue(MockResponse().setResponseCode(200))

        client().newCall(Request.Builder().url(mockServer.url("/api/resource")).build())
            .execute().close()

        assertEquals("valid-token", store.accessToken())
        assertEquals(0, store.clearCallCount)
    }

    @Test
    fun `403 keeps session and emits SESSION_FORBIDDEN`() {
        store.saveTokens("valid-token", null)
        mockServer.enqueue(MockResponse().setResponseCode(403))

        client().newCall(Request.Builder().url(mockServer.url("/api/streaks/protect")).build())
            .execute().close()

        assertEquals("valid-token", store.accessToken())
        assertEquals(0, store.clearCallCount)
        assertEquals(1, sessionEvents.recordedEvents().count { it == SessionEvent.SESSION_FORBIDDEN })
    }

    @Test
    fun `401 on logout does not emit SESSION_EXPIRED`() {
        store.saveTokens("expired-token", null)
        mockServer.enqueue(MockResponse().setResponseCode(401))

        client().newCall(Request.Builder().url(mockServer.url("/api/auth/logout")).build())
            .execute().close()

        assertTrue(sessionEvents.recordedEvents().isEmpty())
    }

    @Test
    fun `ApiRoutes recognizes logout as exempt from session expiry`() {
        assertTrue(ApiRoutes.isAuthExemptRequest("/api/auth/logout"))
        assertTrue(ApiRoutes.isAuthExemptRequest("/api/Auth/Logout"))
    }
}
