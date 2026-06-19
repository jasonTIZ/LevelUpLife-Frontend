package com.example.leveluplife.data.player

data class PlayerProfile(
    val playerUserId: String,
    val userName: String,
    val level: Int,
    val classId: Int = 0,
    val className: String = "",
    val name: String,
    val lastName: String,
    val email: String,
    val birthdate: String? = null,
    val avatarUri: String? = null,
    val bio: String = "",
)

data class ProfileFetchResult(
    val profile: PlayerProfile,
    val etag: String,
)

data class ProfileUpdateResult(
    val profile: PlayerProfile,
    val etag: String,
)
