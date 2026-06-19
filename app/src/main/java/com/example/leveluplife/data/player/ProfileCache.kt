package com.example.leveluplife.data.player

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.leveluplife.data.auth.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "profile_cache_prefs",
)

interface ProfileCache {
    val profile: StateFlow<PlayerProfile?>
    suspend fun loadPersisted()
    suspend fun update(profile: PlayerProfile, etag: String? = null)
    suspend fun updateLocalExtras(avatarUri: String?, bio: String)
    /** Clears in-memory state only; per-user disk cache is kept for the same account on re-login. */
    suspend fun clearMemory()
    /** Wipes all persisted profile entries (tests / account reset). */
    suspend fun clear()
    fun currentEtag(): String?
}

class DefaultProfileCache(
    context: Context,
    private val avatarStorage: ProfileAvatarStorage,
    private val tokenStore: TokenStore,
) : ProfileCache {
    private val dataStore = context.applicationContext.profileDataStore
    private val _profile = MutableStateFlow<PlayerProfile?>(null)
    override val profile: StateFlow<PlayerProfile?> = _profile.asStateFlow()

    private var etag: String? = null

    override suspend fun loadPersisted() {
        val scopeId = sessionScopeId() ?: return
        val prefs = dataStore.data.first()
        migrateLegacyPrefsIfNeeded(prefs, scopeId)
        val loaded = readScopedProfile(dataStore.data.first(), scopeId) ?: return
        _profile.value = loaded.profile
        etag = loaded.etag
    }

    override suspend fun update(profile: PlayerProfile, etag: String?) {
        val scopeId = sessionScopeId() ?: profile.playerUserId.takeIf { it.isNotBlank() } ?: return
        if (etag != null) this.etag = etag
        _profile.update { profile }
        dataStore.edit { prefs ->
            writeScopedProfile(prefs, scopeId, profile, etag ?: this.etag)
        }
    }

    override suspend fun updateLocalExtras(avatarUri: String?, bio: String) {
        val current = _profile.value ?: return
        update(current.copy(avatarUri = avatarUri, bio = bio))
    }

    override suspend fun clearMemory() {
        etag = null
        _profile.value = null
    }

    override suspend fun clear() {
        clearMemory()
        dataStore.edit { it.clear() }
    }

    override fun currentEtag(): String? = etag

    private fun sessionScopeId(): String? = tokenStore.userId()?.takeIf { it.isNotBlank() }

    private data class ScopedProfileSnapshot(
        val profile: PlayerProfile,
        val etag: String?,
    )

    private fun readScopedProfile(prefs: Preferences, userId: String): ScopedProfileSnapshot? {
        val userName = prefs[scopedKey(userId, Suffix.USER_NAME)] ?: return null
        return ScopedProfileSnapshot(
            profile = PlayerProfile(
                playerUserId = prefs[scopedKey(userId, Suffix.PLAYER_ID)].orEmpty().ifBlank { userId },
                userName = userName,
                level = prefs[scopedKey(userId, Suffix.LEVEL)]?.toIntOrNull() ?: 1,
                classId = prefs[scopedKey(userId, Suffix.CLASS_ID)]?.toIntOrNull() ?: 0,
                className = prefs[scopedKey(userId, Suffix.CLASS_NAME)].orEmpty(),
                name = prefs[scopedKey(userId, Suffix.NAME)].orEmpty(),
                lastName = prefs[scopedKey(userId, Suffix.LAST_NAME)].orEmpty(),
                email = prefs[scopedKey(userId, Suffix.EMAIL)].orEmpty(),
                birthdate = prefs[scopedKey(userId, Suffix.BIRTHDATE)],
                avatarUri = avatarStorage.resolveDisplayUri(prefs[scopedKey(userId, Suffix.AVATAR_URI)]),
                bio = prefs[scopedKey(userId, Suffix.BIO)].orEmpty(),
            ),
            etag = prefs[scopedKey(userId, Suffix.ETAG)],
        )
    }

    private fun writeScopedProfile(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        userId: String,
        profile: PlayerProfile,
        etag: String?,
    ) {
        prefs[scopedKey(userId, Suffix.PLAYER_ID)] = profile.playerUserId
        prefs[scopedKey(userId, Suffix.USER_NAME)] = profile.userName
        prefs[scopedKey(userId, Suffix.LEVEL)] = profile.level.toString()
        prefs[scopedKey(userId, Suffix.CLASS_ID)] = profile.classId.toString()
        prefs[scopedKey(userId, Suffix.CLASS_NAME)] = profile.className
        prefs[scopedKey(userId, Suffix.NAME)] = profile.name
        prefs[scopedKey(userId, Suffix.LAST_NAME)] = profile.lastName
        prefs[scopedKey(userId, Suffix.EMAIL)] = profile.email
        profile.birthdate?.let { prefs[scopedKey(userId, Suffix.BIRTHDATE)] = it }
            ?: prefs.remove(scopedKey(userId, Suffix.BIRTHDATE))
        profile.avatarUri?.let { prefs[scopedKey(userId, Suffix.AVATAR_URI)] = it }
            ?: prefs.remove(scopedKey(userId, Suffix.AVATAR_URI))
        prefs[scopedKey(userId, Suffix.BIO)] = profile.bio
        etag?.let { prefs[scopedKey(userId, Suffix.ETAG)] = it }
    }

    private suspend fun migrateLegacyPrefsIfNeeded(prefs: Preferences, sessionUserId: String) {
        val legacyUserId = prefs[LEGACY_KEY_PLAYER_ID].orEmpty()
        if (legacyUserId.isBlank() || legacyUserId != sessionUserId) return
        val legacyUserName = prefs[LEGACY_KEY_USER_NAME] ?: return
        val profile = PlayerProfile(
            playerUserId = legacyUserId,
            userName = legacyUserName,
            level = prefs[LEGACY_KEY_LEVEL]?.toIntOrNull() ?: 1,
            classId = prefs[LEGACY_KEY_CLASS_ID]?.toIntOrNull() ?: 0,
            className = prefs[LEGACY_KEY_CLASS_NAME].orEmpty(),
            name = prefs[LEGACY_KEY_NAME].orEmpty(),
            lastName = prefs[LEGACY_KEY_LAST_NAME].orEmpty(),
            email = prefs[LEGACY_KEY_EMAIL].orEmpty(),
            birthdate = prefs[LEGACY_KEY_BIRTHDATE],
            avatarUri = avatarStorage.resolveDisplayUri(prefs[LEGACY_KEY_AVATAR_URI]),
            bio = prefs[LEGACY_KEY_BIO].orEmpty(),
        )
        val legacyEtag = prefs[LEGACY_KEY_ETAG]
        dataStore.edit { mutablePrefs ->
            writeScopedProfile(mutablePrefs, sessionUserId, profile, legacyEtag)
            mutablePrefs.remove(LEGACY_KEY_PLAYER_ID)
            mutablePrefs.remove(LEGACY_KEY_USER_NAME)
            mutablePrefs.remove(LEGACY_KEY_LEVEL)
            mutablePrefs.remove(LEGACY_KEY_CLASS_ID)
            mutablePrefs.remove(LEGACY_KEY_CLASS_NAME)
            mutablePrefs.remove(LEGACY_KEY_NAME)
            mutablePrefs.remove(LEGACY_KEY_LAST_NAME)
            mutablePrefs.remove(LEGACY_KEY_EMAIL)
            mutablePrefs.remove(LEGACY_KEY_BIRTHDATE)
            mutablePrefs.remove(LEGACY_KEY_AVATAR_URI)
            mutablePrefs.remove(LEGACY_KEY_BIO)
            mutablePrefs.remove(LEGACY_KEY_ETAG)
        }
    }

    private fun scopedKey(userId: String, suffix: String) =
        stringPreferencesKey("user_${userId}_$suffix")

    private object Suffix {
        const val PLAYER_ID = "player_id"
        const val USER_NAME = "user_name"
        const val LEVEL = "level"
        const val CLASS_ID = "class_id"
        const val CLASS_NAME = "class_name"
        const val NAME = "name"
        const val LAST_NAME = "last_name"
        const val EMAIL = "email"
        const val BIRTHDATE = "birthdate"
        const val AVATAR_URI = "avatar_uri"
        const val BIO = "bio"
        const val ETAG = "etag"
    }

    private companion object {
        val LEGACY_KEY_PLAYER_ID = stringPreferencesKey("player_id")
        val LEGACY_KEY_USER_NAME = stringPreferencesKey("user_name")
        val LEGACY_KEY_LEVEL = stringPreferencesKey("level")
        val LEGACY_KEY_CLASS_ID = stringPreferencesKey("class_id")
        val LEGACY_KEY_CLASS_NAME = stringPreferencesKey("class_name")
        val LEGACY_KEY_NAME = stringPreferencesKey("name")
        val LEGACY_KEY_LAST_NAME = stringPreferencesKey("last_name")
        val LEGACY_KEY_EMAIL = stringPreferencesKey("email")
        val LEGACY_KEY_BIRTHDATE = stringPreferencesKey("birthdate")
        val LEGACY_KEY_AVATAR_URI = stringPreferencesKey("avatar_uri")
        val LEGACY_KEY_BIO = stringPreferencesKey("bio")
        val LEGACY_KEY_ETAG = stringPreferencesKey("etag")
    }
}
