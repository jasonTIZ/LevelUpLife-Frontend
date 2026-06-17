package com.example.leveluplife.ui.updatetask

import com.example.leveluplife.data.network.dto.HabitTaskDto
import com.example.leveluplife.ui.createtask.HabitTaskFormState

data class UpdateHabitTaskUiState(
    val form: HabitTaskFormState = HabitTaskFormState(),
    val originalStartDate: String? = null,
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val isSubmitting: Boolean = false,
    val updatedTask: HabitTaskDto? = null,
    val showConflictDialog: Boolean = false,
)
