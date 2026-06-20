package com.example.leveluplife.ui.habitdetail

import com.example.leveluplife.data.network.dto.HabitDisciplineDto
import com.example.leveluplife.data.network.dto.HabitDto
import com.example.leveluplife.ui.createtask.HabitTaskFormState

data class HabitDetailUiState(
    val habit: HabitDto? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isEditing: Boolean = false,
    val editTitle: String = "",
    val editDescription: String = "",
    val editTitleError: String? = null,
    val newTasks: List<HabitTaskFormState> = emptyList(),
    val disciplines: List<HabitDisciplineDto> = emptyList(),
    val isDisciplinesLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val updateSuccess: Boolean = false,
)
