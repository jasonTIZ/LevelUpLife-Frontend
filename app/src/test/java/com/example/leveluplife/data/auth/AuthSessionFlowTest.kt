package com.example.leveluplife.data.auth

import com.example.leveluplife.data.network.AuthApi
import com.example.leveluplife.data.network.NetworkModule
import com.example.leveluplife.data.network.interceptor.AuthInterceptor
import com.example.leveluplife.ui.profile.FakeProfileCache
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
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
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Escenarios de sesión del contrato de auth, sin backend real (MockWebServer).
 */
class AuthSessionFlowTest {

    private val mockServer = MockWebServer()
    private lateinit var store: FakeTokenStore
    private lateinit var sessionEvents: SessionEvents
    private lateinit var repo: AuthRepository
    private lateinit var httpClient: OkHttpClient

    @Before
    fun setUp() {
        mockServer.start()
        store = FakeTokenStore()
        sessionEvents = SessionEvents()
        val json = NetworkModule.jsonParser()
        httpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(store, sessionEvents))
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockServer.url("/"))
            .client(httpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        repo = DefaultAuthRepository(
            api = retrofit.create(AuthApi::class.java),
            tokenStore = store,
            sessionEvents = sessionEvents,
            profileCache = FakeProfileCache(),
            json = json,
        )
    }

    @After
    fun tearDown() = mockServer.shutdown()

    // Escenario 1 — Login y request autenticado
    @Test
    fun `login exitoso y request con Bearer recibe 200 y emite SESSION_LOGIN_SUCCESS`() = runTest {
        mockServer.enqueue(loginOkResponse("jwt-valid"))
        mockServer.enqueue(MockResponse().setResponseCode(200))

        val loginResult = repo.login("user@test.com", "pass")
        assertTrue(loginResult.isSuccess)

        httpClient.newCall(Request.Builder().url(mockServer.url("/api/player/profile")).build())
            .execute().close()

        mockServer.takeRequest()
        val profileRequest = mockServer.takeRequest()
        assertEquals("Bearer jwt-valid", profileRequest.getHeader("Authorization"))
        assertEquals(1, sessionEvents.recordedEvents().count { it == SessionEvent.SESSION_LOGIN_SUCCESS })
        assertTrue(repo.isLoggedIn())
    }

    // Escenario 2 — Token expirado fuerza logout
    @Test
    fun `401 en endpoint protegido limpia credenciales y emite SESSION_EXPIRED sin refresh`() {
        store.saveTokens("jwt-expired", null)
        mockServer.enqueue(MockResponse().setResponseCode(401))

        httpClient.newCall(Request.Builder().url(mockServer.url("/api/habit-tasks")).build())
            .execute().close()

        assertNull(store.accessToken())
        assertEquals(1, store.clearCallCount)
        assertEquals(1, sessionEvents.recordedEvents().count { it == SessionEvent.SESSION_EXPIRED })
        assertFalse(sessionEvents.recordedEvents().any { it == SessionEvent.SESSION_LOGOUT })
        assertFalse(repo.isLoggedIn())
    }

    // Escenario 3 — Logout limpia storage
    @Test
    fun `logout llama POST api auth logout borra token y emite SESSION_LOGOUT`() = runTest {
        mockServer.enqueue(loginOkResponse("jwt-active"))
        repo.login("user@test.com", "pass")
        sessionEvents.clearRecordedEvents()
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"success":true,"message":"Sesión cerrada correctamente."}"""),
        )

        repo.logout()

        mockServer.takeRequest()
        val logoutRequest = mockServer.takeRequest()
        assertEquals("POST", logoutRequest.method)
        assertTrue(logoutRequest.path!!.endsWith("/api/auth/logout"))
        assertEquals("Bearer jwt-active", logoutRequest.getHeader("Authorization"))
        assertNull(store.accessToken())
        assertFalse(repo.isLoggedIn())
        assertEquals(1, sessionEvents.recordedEvents().count { it == SessionEvent.SESSION_LOGOUT })
    }

    // Escenario 4 — 403 no debe cerrar sesión
    @Test
    fun `403 en endpoint protegido mantiene sesión y emite SESSION_FORBIDDEN`() {
        store.saveTokens("jwt-valid", null)
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(403)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"code":403,"message":"Forbidden"}"""),
        )

        httpClient.newCall(Request.Builder().url(mockServer.url("/api/streaks/protect")).build())
            .execute().close()

        assertEquals("jwt-valid", store.accessToken())
        assertEquals(0, store.clearCallCount)
        assertTrue(repo.isLoggedIn())
        assertEquals(1, sessionEvents.recordedEvents().count { it == SessionEvent.SESSION_FORBIDDEN })
        assertFalse(
            sessionEvents.recordedEvents().any {
                it == SessionEvent.SESSION_EXPIRED || it == SessionEvent.SESSION_LOGOUT
            },
        )
    }

    @Test
    fun `401 en login no emite SESSION_EXPIRED`() = runTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"success":false,"message":"Credenciales inválidas."}"""),
        )

        val result = repo.login("user@test.com", "wrong")

        assertTrue(result.isFailure)
        assertFalse(sessionEvents.recordedEvents().any { it == SessionEvent.SESSION_EXPIRED })
    }

    private fun loginOkResponse(token: String) = MockResponse()
        .setResponseCode(200)
        .addHeader("Content-Type", "application/json")
        .setBody(
            """{"success":true,"data":{"token":"$token","userName":"TestUser","level":1,"className":"Warrior"}}""",
        )
}
