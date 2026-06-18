package com.example.leveluplife.ui.habittaskdetail

import com.example.leveluplife.data.network.dto.HabitTaskDto

data class HabitTaskDetailUiState(
    val task: HabitTaskDto? = null,
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val isCompleting: Boolean = false,
    val completionError: String? = null,
    val reward: TaskCompletionReward? = null,
)
