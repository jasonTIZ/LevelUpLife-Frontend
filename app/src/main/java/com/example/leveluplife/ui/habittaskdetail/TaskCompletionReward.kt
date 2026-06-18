package com.example.leveluplife.ui.habittaskdetail

data class TaskCompletionReward(
    val taskTitle: String,
    val xpEarned: Int,
    val newLevel: Int,
    val previousLevel: Int,
    val streakUpdated: Boolean,
) {
    val leveledUp: Boolean
        get() = newLevel > previousLevel
}
