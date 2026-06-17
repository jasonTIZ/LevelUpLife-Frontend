package com.example.leveluplife.ui.profile

import com.example.leveluplife.data.player.PlayerProfile
import com.example.leveluplife.data.player.ProfileAvatarStorage
import com.example.leveluplife.data.player.ProfileCache
import com.example.leveluplife.data.player.ProfileError
import com.example.leveluplife.data.player.ProfileException
import com.example.leveluplife.data.player.ProfileFetchResult
import com.example.leveluplife.data.player.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeProfileRepository(
    private var fetchResult: Result<ProfileFetchResult> = Result.failure(Exception("not configured")),
    private var updateResult: Result<PlayerProfile> = Result.failure(Exception("not configured")),
) : ProfileRepository {
    var fetchCalls = 0
        private set
    var updateCalls = 0
        private set
    var lastUpdateEtag: String? = null
        private set

    fun setFetchResult(result: Result<ProfileFetchResult>) {
        fetchResult = result
    }

    fun setUpdateResult(result: Result<PlayerProfile>) {
        updateResult = result
    }

    override suspend fun fetchProfile(): Result<ProfileFetchResult> {
        fetchCalls++
        return fetchResult
    }

    override suspend fun updateProfile(
        etag: String,
        name: String,
        lastName: String,
        email: String,
        birthdate: String?,
        userName: String,
    ): Result<PlayerProfile> {
        updateCalls++
        lastUpdateEtag = etag
        return updateResult
    }
}

class FakeProfileCache(
    initial: PlayerProfile? = null,
    private var etag: String? = "\"etag-1\"",
) : ProfileCache {
    private val _profile = MutableStateFlow(initial)
    override val profile: StateFlow<PlayerProfile?> = _profile

    override suspend fun loadPersisted() = Unit

    override suspend fun update(profile: PlayerProfile, etag: String?) {
        if (etag != null) this.etag = etag
        _profile.value = profile
    }

    override suspend fun updateLocalExtras(avatarUri: String?, bio: String) {
        val current = _profile.value ?: return
        _profile.value = current.copy(avatarUri = avatarUri, bio = bio)
    }

    override fun currentEtag(): String? = etag

    override suspend fun clear() {
        etag = null
        _profile.value = null
    }
}

fun sampleProfile() = PlayerProfile(
    playerUserId = "1",
    userName = "aarontest",
    level = 3,
    classId = 1,
    className = "Warrior",
    name = "Aaron",
    lastName = "Test",
    email = "aarontest@leveluplife.com",
    birthdate = "2000-01-15",
    bio = "Hola",
)

fun validationException() = ProfileException(
    ProfileError.Validation(
        message = "Validation failed",
        fieldErrors = mapOf(
            com.example.leveluplife.data.player.ProfileField.Email to "Invalid email",
        ),
    ),
)

class FakeProfileAvatarStorage(
    private val persistedUri: String = "file:///fake/profile_avatar.jpg",
) : ProfileAvatarStorage {
    var persistCalls = 0
        private set

    override suspend fun persistFromPickerUri(sourceUri: String, mimeType: String?): String? {
        persistCalls++
        return persistedUri
    }

    override fun resolveDisplayUri(storedUri: String?): String? = storedUri ?: persistedUri
}
