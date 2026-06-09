package com.example.leveluplife.ui.habit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.leveluplife.data.network.dto.CreateHabitTaskRequestDto
import com.example.leveluplife.data.network.dto.TaskCompletionCriteria
import com.example.leveluplife.data.network.dto.TaskDifficulty
import com.example.leveluplife.data.network.dto.TaskFrequency
import com.example.leveluplife.data.network.dto.TaskPeriodUnit

data class CreateHabitUiState(
    val title: String = "",
    val description: String = "",
    val disciplineId: Int? = null,
    val tasks: List<TaskUiState> = listOf(TaskUiState()),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

data class TaskUiState(
    val title: String = "",
    val description: String = "",
    val habitDisciplineId: Int? = null,
    val weekDays: String = "",
    val difficulty: TaskDifficulty = TaskDifficulty.EASY,
    val frequency: TaskFrequency = TaskFrequency.DAILY,
    val periodLength: Int = 1,
    val periodUnit: TaskPeriodUnit = TaskPeriodUnit.DAYS,
    val startDate: String = "",
    val completionCriteria: TaskCompletionCriteria = TaskCompletionCriteria.REPETITIONS,
    val evidence: String? = null,
    val repetitionCriteria: RepetitionCriteriaUiState? = RepetitionCriteriaUiState(),
    val xpValue: Int? = null,
    val isActive: Boolean = true,
    val errors: Map<String, String> = emptyMap()
)

data class RepetitionCriteriaUiState(
    val repetitions: Int = 1,
    val measurementUnit: String = "",
    val isPartialAllowed: Boolean? = null,
    val isActive: Boolean? = null
)
