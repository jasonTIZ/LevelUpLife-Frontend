package com.example.leveluplife.ui.habit

import com.example.leveluplife.data.network.dto.HabitCategoryDto
import com.example.leveluplife.data.network.dto.HabitDisciplineDto
import com.example.leveluplife.ui.createtask.HabitTaskFormState

data class CreateHabitUiState(
    val title: String = "",
    val description: String = "",
    val categories: List<HabitCategoryDto> = emptyList(),
    val selectedCategoryId: Int? = null,
    val isCategoriesLoading: Boolean = false,
    val allDisciplines: List<HabitDisciplineDto> = emptyList(),
    val disciplines: List<HabitDisciplineDto> = emptyList(),
    val isDisciplinesLoading: Boolean = false,
    val tasks: List<HabitTaskFormState> = listOf(HabitTaskFormState()),
    val habitTitleError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)
