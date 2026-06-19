package com.example.leveluplife.ui.habit

import com.example.leveluplife.data.network.dto.HabitDisciplineDto
import com.example.leveluplife.ui.createtask.HabitTaskFormState

data class CreateHabitUiState(
    val title: String = "",
    val description: String = "",
    val disciplineId: Int? = null,
    val disciplines: List<HabitDisciplineDto> = emptyList(),
    val isDisciplinesLoading: Boolean = false,
    val tasks: List<HabitTaskFormState> = listOf(HabitTaskFormState()),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)
