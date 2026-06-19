package com.example.leveluplife.ui.profile

import com.example.leveluplife.data.player.PlayerProfile
import com.example.leveluplife.data.player.ProfileCache
import com.example.leveluplife.data.player.ProfileError
import com.example.leveluplife.data.player.ProfileException
import com.example.leveluplife.data.player.ProfileFetchResult
import com.example.leveluplife.data.player.ProfileRepository
import com.example.leveluplife.data.player.ProfileUpdateResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeProfileRepository(
    private var fetchResult: Result<ProfileFetchResult> = Result.failure(Exception("not configured")),
    private var updateResult: Result<PlayerProfile> = Result.failure(Exception("not configured")),
    private var uploadResult: Result<ProfileUpdateResult> = Result.failure(Exception("not configured")),
) : ProfileRepository {
    var fetchCalls = 0
        private set
    var updateCalls = 0
        private set
    var uploadCalls = 0
        private set
    var lastUpdateEtag: String? = null
        private set
    var lastBio: String? = null
        private set

    fun setFetchResult(result: Result<ProfileFetchResult>) {
        fetchResult = result
    }

    fun setUpdateResult(result: Result<PlayerProfile>) {
        updateResult = result
    }

    fun setUploadResult(result: Result<ProfileUpdateResult>) {
        uploadResult = result
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
        bio: String,
    ): Result<PlayerProfile> {
        updateCalls++
        lastUpdateEtag = etag
        lastBio = bio
        return updateResult
    }

    override suspend fun uploadAvatar(
        etag: String,
        sourceUri: String,
        mimeType: String?,
    ): Result<ProfileUpdateResult> {
        uploadCalls++
        return uploadResult
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

    override suspend fun clearMemory() {
        _profile.value = null
        etag = null
    }

    override suspend fun clear() {
        clearMemory()
    }

    override fun currentEtag(): String? = etag
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
