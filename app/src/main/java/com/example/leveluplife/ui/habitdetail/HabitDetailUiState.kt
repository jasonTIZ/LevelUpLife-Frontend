package com.example.leveluplife.ui.habitdetail

import com.example.leveluplife.data.network.dto.HabitDto

data class HabitDetailUiState(
    val habit: HabitDto? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showInactiveTasks: Boolean = false,
)
