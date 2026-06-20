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
    val totalExperiencePoints: Int = 0,
    val experiencePointsInCurrentLevel: Int = 0,
    val experiencePointsRequiredForNextLevel: Int = 0,
    val levelProgressPercent: Double = 0.0,
    val daysStreak: Int = 0,
    val gold: Int = 0,
) {
    val levelProgressFraction: Float
        get() = levelProgressPercent.coerceIn(0.0, 1.0).toFloat()
}

data class ProfileFetchResult(
    val profile: PlayerProfile,
    val etag: String,
)

data class ProfileUpdateResult(
    val profile: PlayerProfile,
    val etag: String,
)
