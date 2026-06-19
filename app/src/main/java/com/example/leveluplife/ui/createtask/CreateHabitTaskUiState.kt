package com.example.leveluplife.ui.createtask

import com.example.leveluplife.data.network.dto.HabitDto
import com.example.leveluplife.data.network.dto.HabitTaskDto
import java.time.LocalDate

data class CreateHabitTaskUiState(
    val form: HabitTaskFormState = HabitTaskFormState(
        startDate = LocalDate.now().toString(),
    ),
    val isLoadingHabits: Boolean = true,
    val habitsLoadError: String? = null,
    val isSubmitting: Boolean = false,
    val createdTask: HabitTaskDto? = null,
) {
    val habits: List<HabitDto> get() = form.habits
    val selectedHabitId: Int? get() = form.selectedHabitId
    val selectedHabit: HabitDto? get() = form.selectedHabit
    val title get() = form.title
    val description get() = form.description
    val difficulty get() = form.difficulty
    val frequency get() = form.frequency
    val periodLength get() = form.periodLength
    val periodUnit get() = form.periodUnit
    val startDate get() = form.startDate
    val completionCriteria get() = form.completionCriteria
    val repetitions get() = form.repetitions
    val measurementUnit get() = form.measurementUnit
    val evidence get() = form.evidence
    val isPartialAllowed get() = form.isPartialAllowed
    val selectedTemplateId get() = form.selectedTemplateId
    val fieldErrors get() = form.fieldErrors
    val showValidationErrors get() = form.showValidationErrors
    val submitError get() = form.submitError
}
