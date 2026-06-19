package com.example.leveluplife.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.leveluplife.data.auth.TokenStore
import com.example.leveluplife.data.player.PlayerProfile
import com.example.leveluplife.data.player.ProfileFetchResult
import com.example.leveluplife.ui.theme.LevelUpLifeTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenLogoutTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun confirmingLogoutInvokesCallback() {
        var logoutInvoked = false
        val viewModel = ProfileViewModel(
            profileRepository = FakeLogoutProfileRepository(),
            profileCache = FakeLogoutProfileCache(sampleProfile()),
            tokenStore = FakeLogoutTokenStore(),
        )

        composeRule.setContent {
            LevelUpLifeTheme {
                ProfileScreen(
                    viewModel = viewModel,
                    onBack = {},
                    onLogout = { logoutInvoked = true },
                )
            }
        }

        composeRule.onNodeWithTag(ProfileTestTags.LOGOUT_BUTTON).performClick()
        composeRule.onNodeWithTag(ProfileTestTags.LOGOUT_CONFIRM_DIALOG).assertIsDisplayed()
        composeRule.onNodeWithTag(ProfileTestTags.LOGOUT_CONFIRM_BUTTON).performClick()

        assertTrue(logoutInvoked)
    }

    private class FakeLogoutProfileRepository : com.example.leveluplife.data.player.ProfileRepository {
        override suspend fun fetchProfile() = Result.success(
            ProfileFetchResult(profile = sampleProfile(), etag = "\"etag-1\""),
        )

        override suspend fun updateProfile(
            etag: String,
            name: String,
            lastName: String,
            email: String,
            birthdate: String?,
            userName: String,
            bio: String,
        ) = Result.failure(IllegalStateException("not configured"))

        override suspend fun uploadAvatar(
            etag: String,
            sourceUri: String,
            mimeType: String?,
        ) = Result.failure(IllegalStateException("not configured"))
    }

    private class FakeLogoutProfileCache(
        initial: PlayerProfile?,
    ) : com.example.leveluplife.data.player.ProfileCache {
        private val _profile = MutableStateFlow(initial)
        override val profile: StateFlow<PlayerProfile?> = _profile

        override suspend fun loadPersisted() = Unit

        override suspend fun update(profile: PlayerProfile, etag: String?) {
            _profile.value = profile
        }

        override suspend fun updateLocalExtras(avatarUri: String?, bio: String) = Unit

        override fun clearMemory() {
            _profile.value = null
        }

        override suspend fun clear() {
            clearMemory()
        }

        override fun currentEtag(): String? = "\"etag-1\""
    }

    private class FakeLogoutTokenStore : TokenStore {
        override fun accessToken(): String? = "token"
        override fun refreshToken(): String? = null
        override fun userId(): String? = "1"
        override fun hasSession(): Boolean = true
        override fun saveTokens(accessToken: String, refreshToken: String?, userId: String?) = Unit
        override fun clear() = Unit
    }
}
