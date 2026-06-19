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
    private lateinit var profileCache: FakeProfileCache
    private lateinit var repo: AuthRepository

    @Before
    fun setUp() {
        mockServer.start()
        store = FakeTokenStore()
        profileCache = FakeProfileCache()
        val json = NetworkModule.jsonParser()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockServer.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        repo = DefaultAuthRepository(
            api = retrofit.create(AuthApi::class.java),
            tokenStore = store,
            profileCache = profileCache,
            json = json,
        )
    }

    @After
    fun tearDown() = mockServer.shutdown()

    // Scenario: Tokens stored securely
    // Given login returns tokens
    // When client stores them
    // Then tokens are saved using secure storage APIs and not in plain localStorage
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

    // Scenario: Tokens cleared on logout
    // Given logout initiated
    // When logout completes
    // Then secure storage is cleared of tokens and session data
    @Test
    fun `login exitoso limpia la memoria del perfil sin borrar datos locales del usuario`() = runTest {
        profileCache.update(sampleProfile(), "\"etag-1\"")

        mockServer.enqueue(loginOkResponse("tok-abc"))
        val result = repo.login("user@test.com", "pass")

        assertTrue(result.isSuccess)
        assertNull(profileCache.profile.value)
    }

    @Test
    fun `logout limpia todos los tokens y la sesión del almacenamiento seguro`() = runTest {
        mockServer.enqueue(loginOkResponse("tok-abc"))
        repo.login("user@test.com", "pass")
        assertTrue("precondición: debe estar logueado antes del logout", repo.isLoggedIn())

        repo.logout()

        assertEquals(1, store.clearCallCount)
        assertNull(store.accessToken())
        assertNull(store.refreshToken())
        assertNull(profileCache.profile.value)
        assertFalse(repo.isLoggedIn())
    }

    @Test
    fun `logout sin sesión previa no falla y el almacenamiento queda limpio`() = runTest {
        profileCache.update(sampleProfile(), "\"etag-1\"")
        repo.logout()

        assertEquals(1, store.clearCallCount)
        assertNull(store.accessToken())
        assertNull(profileCache.profile.value)
        assertFalse(repo.isLoggedIn())
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
