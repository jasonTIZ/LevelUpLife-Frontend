package com.example.leveluplife.ui.createtask

import com.example.leveluplife.data.network.dto.HabitDto
import com.example.leveluplife.data.network.dto.HabitTaskDto
import com.example.leveluplife.domain.validation.HabitTaskFormErrors

data class CreateHabitTaskUiState(
    val habits: List<HabitDto> = emptyList(),
    val isLoadingHabits: Boolean = true,
    val habitsLoadError: String? = null,
    val selectedHabitId: Int? = null,
    val title: String = "",
    val description: String = "",
    val difficulty: String? = "MEDIUM",
    val frequency: String? = "WEEKLY",
    val periodLength: String = "1",
    val periodUnit: String? = "WEEKS",
    val startDate: String = "",
    val completionCriteria: String? = "REPETITIONS",
    val repetitions: String = "3",
    val measurementUnit: String? = "SERIES",
    val evidence: String? = null,
    val isPartialAllowed: Boolean = true,
    val selectedTemplateId: String? = null,
    val fieldErrors: HabitTaskFormErrors = HabitTaskFormErrors(),
    val showValidationErrors: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val createdTask: HabitTaskDto? = null,
) {
    val selectedHabit: HabitDto?
        get() = habits.firstOrNull { it.id == selectedHabitId }
}
