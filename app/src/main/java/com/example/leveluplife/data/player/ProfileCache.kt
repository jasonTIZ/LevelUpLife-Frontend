package com.example.leveluplife.data.player

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
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
    suspend fun updateLevel(level: Int)
    fun currentEtag(): String?
}

class DefaultProfileCache(
    context: Context,
    private val avatarStorage: ProfileAvatarStorage,
) : ProfileCache {
    private val dataStore = context.applicationContext.profileDataStore
    private val _profile = MutableStateFlow<PlayerProfile?>(null)
    override val profile: StateFlow<PlayerProfile?> = _profile.asStateFlow()

    private var etag: String? = null

    override suspend fun loadPersisted() {
        val prefs = dataStore.data.first()
        val userName = prefs[KEY_USER_NAME] ?: return
        _profile.value = PlayerProfile(
            playerUserId = prefs[KEY_PLAYER_ID].orEmpty(),
            userName = userName,
            level = prefs[KEY_LEVEL]?.toIntOrNull() ?: 1,
            classId = prefs[KEY_CLASS_ID]?.toIntOrNull() ?: 0,
            className = prefs[KEY_CLASS_NAME].orEmpty(),
            name = prefs[KEY_NAME].orEmpty(),
            lastName = prefs[KEY_LAST_NAME].orEmpty(),
            email = prefs[KEY_EMAIL].orEmpty(),
            birthdate = prefs[KEY_BIRTHDATE],
            avatarUri = avatarStorage.resolveDisplayUri(prefs[KEY_AVATAR_URI]),
            bio = prefs[KEY_BIO].orEmpty(),
        )
        etag = prefs[KEY_ETAG]
    }

    override suspend fun update(profile: PlayerProfile, etag: String?) {
        if (etag != null) this.etag = etag
        _profile.update { profile }
        dataStore.edit { prefs ->
            prefs[KEY_PLAYER_ID] = profile.playerUserId
            prefs[KEY_USER_NAME] = profile.userName
            prefs[KEY_LEVEL] = profile.level.toString()
            prefs[KEY_CLASS_ID] = profile.classId.toString()
            prefs[KEY_CLASS_NAME] = profile.className
            prefs[KEY_NAME] = profile.name
            prefs[KEY_LAST_NAME] = profile.lastName
            prefs[KEY_EMAIL] = profile.email
            profile.birthdate?.let { prefs[KEY_BIRTHDATE] = it } ?: prefs.remove(KEY_BIRTHDATE)
            profile.avatarUri?.let { prefs[KEY_AVATAR_URI] = it } ?: prefs.remove(KEY_AVATAR_URI)
            prefs[KEY_BIO] = profile.bio
            this.etag?.let { prefs[KEY_ETAG] = it }
        }
    }

    override suspend fun updateLocalExtras(avatarUri: String?, bio: String) {
        val current = _profile.value ?: return
        update(current.copy(avatarUri = avatarUri, bio = bio))
    }

    override suspend fun updateLevel(level: Int) {
        val current = _profile.value ?: return
        if (current.level == level) return
        update(current.copy(level = level))
    }

    override fun currentEtag(): String? = etag

    private companion object {
        val KEY_PLAYER_ID = stringPreferencesKey("player_id")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_LEVEL = stringPreferencesKey("level")
        val KEY_CLASS_ID = stringPreferencesKey("class_id")
        val KEY_CLASS_NAME = stringPreferencesKey("class_name")
        val KEY_NAME = stringPreferencesKey("name")
        val KEY_LAST_NAME = stringPreferencesKey("last_name")
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_BIRTHDATE = stringPreferencesKey("birthdate")
        val KEY_AVATAR_URI = stringPreferencesKey("avatar_uri")
        val KEY_BIO = stringPreferencesKey("bio")
        val KEY_ETAG = stringPreferencesKey("etag")
    }
}
