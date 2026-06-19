package com.example.leveluplife.data.auth

import com.example.leveluplife.data.network.AuthApi
import com.example.leveluplife.data.network.NetworkModule
import com.example.leveluplife.ui.profile.FakeProfileCache
import com.example.leveluplife.ui.profile.sampleProfile
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
    private lateinit var profileCache: FakeProfileCache
    private lateinit var repo: AuthRepository

    @Before
    fun setUp() {
        mockServer.start()
        store = FakeTokenStore()
        sessionEvents = SessionEvents()
        profileCache = FakeProfileCache()
        val json = NetworkModule.jsonParser()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockServer.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        repo = DefaultAuthRepository(
            api = retrofit.create(AuthApi::class.java),
            tokenStore = store,
            sessionEvents = sessionEvents,
            profileCache = profileCache,
            json = json,
        )
    }

    @After
    fun tearDown() = mockServer.shutdown()

    @Test
    fun `successful login persists access token in secure TokenStore`() = runTest {
        mockServer.enqueue(loginOkResponse("tok-abc"))

        val result = repo.login("user@test.com", "pass")

        assertTrue(result.isSuccess)
        assertEquals(1, store.saveCallCount)
        assertEquals("tok-abc", store.accessToken())
        assertTrue(repo.isLoggedIn())
    }

    @Test
    fun `successful login emits SESSION_LOGIN_SUCCESS`() = runTest {
        mockServer.enqueue(loginOkResponse("tok-abc"))

        repo.login("user@test.com", "pass")

        assertEquals(1, sessionEvents.recordedEvents().count { it == SessionEvent.SESSION_LOGIN_SUCCESS })
    }

    @Test
    fun `failed login does not write any token to storage`() = runTest {
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
    fun `successful login clears profile memory without deleting local user data`() = runTest {
        profileCache.update(sampleProfile(), "\"etag-1\"")

        mockServer.enqueue(loginOkResponse("tok-abc"))
        val result = repo.login("user@test.com", "pass")

        assertTrue(result.isSuccess)
        assertNull(profileCache.profile.value)
    }

    @Test
    fun `logout calls endpoint clears tokens and emits SESSION_LOGOUT`() = runTest {
        mockServer.enqueue(loginOkResponse("tok-abc"))
        repo.login("user@test.com", "pass")
        sessionEvents.clearRecordedEvents()
        mockServer.enqueue(logoutOkResponse())

        repo.logout()

        mockServer.takeRequest() // login
        val logoutRequest = mockServer.takeRequest()
        assertEquals("POST", logoutRequest.method)
        assertTrue(logoutRequest.path!!.endsWith("/api/auth/logout"))
        assertEquals(1, store.clearCallCount)
        assertNull(store.accessToken())
        assertNull(store.refreshToken())
        assertNull(profileCache.profile.value)
        assertFalse(repo.isLoggedIn())
        assertEquals(1, sessionEvents.recordedEvents().count { it == SessionEvent.SESSION_LOGOUT })
    }

    @Test
    fun `logout without prior session clears storage and emits SESSION_LOGOUT`() = runTest {
        profileCache.update(sampleProfile(), "\"etag-1\"")
        mockServer.enqueue(logoutOkResponse())

        repo.logout()

        assertEquals(1, store.clearCallCount)
        assertNull(store.accessToken())
        assertNull(profileCache.profile.value)
        assertFalse(repo.isLoggedIn())
        assertEquals(1, sessionEvents.recordedEvents().count { it == SessionEvent.SESSION_LOGOUT })
    }

    @Test
    fun `clearLocalSession clears tokens without calling API`() = runTest {
        mockServer.enqueue(loginOkResponse("tok-abc"))
        repo.login("user@test.com", "pass")

        repo.clearLocalSession()

        assertEquals(1, store.clearCallCount)
        assertFalse(repo.isLoggedIn())
        assertEquals(1, mockServer.requestCount)
    }

    @Test
    fun `repository without session reports logged out and null tokens`() {
        assertFalse(repo.isLoggedIn())
        assertNull(repo.currentTokens().first)
        assertNull(repo.currentTokens().second)
    }

    @Test
    fun `register with token clears profile memory and emits SESSION_LOGIN_SUCCESS`() = runTest {
        profileCache.update(sampleProfile(), "\"etag-1\"")
        mockServer.enqueue(registerWithTokenResponse("tok-reg"))

        val result = repo.register(
            name = "Ana",
            lastName = "Garcia",
            email = "ana@test.com",
            birthdate = "2000-01-01",
            userName = "anagarcia",
            password = "secret123",
            classId = 1,
        )

        assertTrue(result.isSuccess)
        assertNull(profileCache.profile.value)
        assertEquals(1, sessionEvents.recordedEvents().count { it == SessionEvent.SESSION_LOGIN_SUCCESS })
    }

    private fun registerWithTokenResponse(token: String) = MockResponse()
        .setResponseCode(200)
        .addHeader("Content-Type", "application/json")
        .setBody(
            """{"success":true,"data":{"token":"$token","userName":"anagarcia","level":1,"className":"Warrior"}}""",
        )

    private fun loginOkResponse(token: String) = MockResponse()
        .setResponseCode(200)
        .addHeader("Content-Type", "application/json")
        .setBody(
            """{"success":true,"data":{"token":"$token","userName":"TestUser","level":1,"className":"Warrior"}}""",
        )

    private fun logoutOkResponse() = MockResponse()
        .setResponseCode(200)
        .addHeader("Content-Type", "application/json")
        .setBody("""{"success":true,"message":"Sesión cerrada correctamente."}""")
}
