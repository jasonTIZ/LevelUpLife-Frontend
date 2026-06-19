package com.example.leveluplife.data.player

import com.example.leveluplife.data.auth.AuthRepository
import com.example.leveluplife.data.auth.RegisterOutcome
import com.example.leveluplife.data.auth.SessionEvent
import com.example.leveluplife.data.auth.SessionEvents
import com.example.leveluplife.data.network.NetworkModule
import com.example.leveluplife.data.network.PlayerApi
import com.example.leveluplife.ui.profile.FakeProfileCache
import com.example.leveluplife.ui.profile.sampleProfile
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class PlayerRepositoryTest {

    private val mockServer = MockWebServer()
    private lateinit var sessionEvents: SessionEvents
    private lateinit var profileCache: FakeProfileCache
    private lateinit var authRepository: RecordingAuthRepository
    private lateinit var repo: PlayerRepository

    @Before
    fun setUp() {
        mockServer.start()
        sessionEvents = SessionEvents()
        profileCache = FakeProfileCache()
        authRepository = RecordingAuthRepository(sessionEvents, profileCache)
        val json = NetworkModule.jsonParser()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockServer.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        repo = DefaultPlayerRepository(
            api = retrofit.create(PlayerApi::class.java),
            authRepository = authRepository,
        )
    }

    @After
    fun tearDown() = mockServer.shutdown()

    @Test
    fun `successful deactivation clears profile cache and emits SESSION_ACCOUNT_DEACTIVATED`() = runTest {
        profileCache.update(sampleProfile(), "\"etag-1\"")
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """{"message":"Cuenta desactivada correctamente","deactivatedAt":"2026-05-29T00:00:00Z"}""",
                ),
        )

        val result = repo.deactivateAccount("Ya no la uso")

        assertTrue(result.isSuccess)
        assertEquals("Cuenta desactivada correctamente", result.getOrNull()?.message)
        assertNull(profileCache.profile.value)
        assertEquals(1, authRepository.clearSessionAfterDeactivationCalls)
        assertEquals(
            "Cuenta desactivada correctamente",
            authRepository.lastDeactivationMessage,
        )
        assertEquals(
            1,
            sessionEvents.recordedEvents().count { it == SessionEvent.SESSION_ACCOUNT_DEACTIVATED },
        )
    }

    private class RecordingAuthRepository(
        private val sessionEvents: SessionEvents,
        private val profileCache: FakeProfileCache,
    ) : AuthRepository {
        var clearSessionAfterDeactivationCalls = 0
        var lastDeactivationMessage: String? = null

        override suspend fun login(email: String, password: String): Result<com.example.leveluplife.data.auth.AuthSession> =
            Result.failure(IllegalStateException("not configured"))

        override suspend fun register(
            name: String,
            lastName: String,
            email: String,
            birthdate: String,
            userName: String,
            password: String,
            classId: Int,
        ): Result<RegisterOutcome> = Result.failure(IllegalStateException("not configured"))

        override fun isLoggedIn(): Boolean = false

        override suspend fun logout() = Unit

        override suspend fun clearSessionAfterAccountDeactivation(message: String?) {
            clearSessionAfterDeactivationCalls++
            lastDeactivationMessage = message
            profileCache.clear()
            sessionEvents.notifyAccountDeactivated(message)
        }

        override fun clearLocalSession() = Unit

        override fun currentTokens(): Pair<String?, String?> = null to null
    }
}
