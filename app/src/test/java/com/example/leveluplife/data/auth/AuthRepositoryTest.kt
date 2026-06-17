package com.example.leveluplife.data.auth

import com.example.leveluplife.data.network.AuthApi
import com.example.leveluplife.data.network.NetworkModule
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
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

class AuthRepositoryTest {

    private val mockServer = MockWebServer()
    private lateinit var store: FakeTokenStore
    private lateinit var sessionEvents: SessionEvents
    private lateinit var repo: AuthRepository

    @Before
    fun setUp() {
        mockServer.start()
        store = FakeTokenStore()
        sessionEvents = SessionEvents()
        val json = NetworkModule.jsonParser()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockServer.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        repo = DefaultAuthRepository(
            api = retrofit.create(AuthApi::class.java),
            tokenStore = store,
            sessionEvents = sessionEvents,
            json = json,
        )
    }

    @After
    fun tearDown() = mockServer.shutdown()

    @Test
    fun `login exitoso persiste el access token en el TokenStore seguro`() = runTest {
        mockServer.enqueue(loginOkResponse("tok-abc"))

        val result = repo.login("user@test.com", "pass")

        assertTrue(result.isSuccess)
        assertEquals(1, store.saveCallCount)
        assertEquals("tok-abc", store.accessToken())
        assertTrue(repo.isLoggedIn())
    }

    @Test
    fun `login exitoso emite SESSION_LOGIN_SUCCESS`() = runTest {
        mockServer.enqueue(loginOkResponse("tok-abc"))

        repo.login("user@test.com", "pass")

        assertEquals(1, sessionEvents.recordedEvents().count { it == SessionEvent.SESSION_LOGIN_SUCCESS })
    }

    @Test
    fun `login fallido no escribe ningún token en el almacenamiento`() = runTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"message":"Credenciales inválidas"}"""),
        )

        val result = repo.login("user@test.com", "wrong")

        assertTrue(result.isFailure)
        assertEquals(0, store.saveCallCount)
        assertNull(store.accessToken())
        assertFalse(repo.isLoggedIn())
    }

    @Test
    fun `logout llama al endpoint y limpia tokens y emite SESSION_LOGOUT`() = runTest {
        mockServer.enqueue(loginOkResponse("tok-abc"))
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
        assertEquals(1, store.clearCallCount)
        assertNull(store.accessToken())
        assertNull(store.refreshToken())
        assertFalse(repo.isLoggedIn())
        assertEquals(1, sessionEvents.recordedEvents().count { it == SessionEvent.SESSION_LOGOUT })
    }

    @Test
    fun `logout sin sesión previa limpia almacenamiento y emite SESSION_LOGOUT`() = runTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"success":true,"message":"Sesión cerrada correctamente."}"""),
        )

        repo.logout()

        assertEquals(1, store.clearCallCount)
        assertNull(store.accessToken())
        assertFalse(repo.isLoggedIn())
    }

    @Test
    fun `clearLocalSession limpia tokens sin llamar al API`() = runTest {
        mockServer.enqueue(loginOkResponse("tok-abc"))
        repo.login("user@test.com", "pass")

        repo.clearLocalSession()

        assertEquals(1, store.clearCallCount)
        assertFalse(repo.isLoggedIn())
        assertEquals(1, mockServer.requestCount)
    }

    @Test
    fun `repositorio sin sesión reporta no logueado y tokens nulos`() {
        assertFalse(repo.isLoggedIn())
        assertNull(repo.currentTokens().first)
        assertNull(repo.currentTokens().second)
    }

    private fun loginOkResponse(token: String) = MockResponse()
        .setResponseCode(200)
        .addHeader("Content-Type", "application/json")
        .setBody(
            """{"success":true,"data":{"token":"$token","userName":"TestUser","level":1,"className":"Warrior"}}""",
        )
}
