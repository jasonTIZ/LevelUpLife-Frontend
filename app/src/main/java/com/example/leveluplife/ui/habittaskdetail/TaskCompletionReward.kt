package com.example.leveluplife.ui.habittaskdetail

data class TaskCompletionReward(
    val taskTitle: String,
    val xpEarned: Int,
    val previousLevel: Int,
    val newLevel: Int,
    val experiencePointsInCurrentLevel: Int,
    val experiencePointsRequiredForNextLevel: Int,
    val levelProgressPercent: Double,
    val leveledUp: Boolean,
    val streakUpdated: Boolean,
) {
    val progressFraction: Float
        get() = levelProgressPercent.coerceIn(0.0, 1.0).toFloat()
}
