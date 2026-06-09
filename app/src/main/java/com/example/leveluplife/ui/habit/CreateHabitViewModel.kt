package com.example.leveluplife.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.network.dto.CreateHabitRequestDto
import com.example.leveluplife.data.network.dto.CreateHabitTaskRequestDto
import com.example.leveluplife.data.network.dto.MeasurementUnit
import com.example.leveluplife.data.network.dto.RepetitionCriteriaRequestDto
import com.example.leveluplife.domain.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateHabitViewModel(
    private val repository: HabitRepository
) : ViewModel() {

    companion object {
        fun Factory(repository: HabitRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CreateHabitViewModel(repository) as T
            }
        }
    }

    private val _uiState = MutableStateFlow(CreateHabitUiState())
    val uiState: StateFlow<CreateHabitUiState> = _uiState.asStateFlow()

    fun setTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun setDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun setDisciplineId(disciplineId: Int) {
        _uiState.value = _uiState.value.copy(disciplineId = disciplineId)
    }

    fun addTask() {
        val newTask = TaskUiState()
        val updatedTasks = _uiState.value.tasks + newTask
        _uiState.value = _uiState.value.copy(tasks = updatedTasks)
    }

    fun removeTask(index: Int) {
        if (_uiState.value.tasks.size > 1) {
            val updatedTasks = _uiState.value.tasks.toMutableList().apply { removeAt(index) }
            _uiState.value = _uiState.value.copy(tasks = updatedTasks)
        }
    }

    fun updateTask(index: Int, updates: (TaskUiState) -> TaskUiState) {
        val updatedTasks = _uiState.value.tasks.toMutableList().apply {
            this[index] = updates(this[index])
        }
        _uiState.value = _uiState.value.copy(tasks = updatedTasks)
    }

    fun updateRepetitionCriteria(
        taskIndex: Int,
        updates: (RepetitionCriteriaUiState) -> RepetitionCriteriaUiState
    ) {
        val task = _uiState.value.tasks[taskIndex]
        val newRepetitionCriteria = updates(task.repetitionCriteria ?: RepetitionCriteriaUiState())
        updateTask(taskIndex) { it.copy(repetitionCriteria = newRepetitionCriteria) }
    }

    fun createHabit() {
        val state = _uiState.value
        val userId = repository.getCurrentUserId()
        val validationError = validateHabit(state, userId)
        if (validationError != null) {
            _uiState.value = state.copy(error = validationError)
            return
        }

        _uiState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val request = buildCreateHabitRequest(state, userId)
            val result = repository.createHabit(request)

            _uiState.value = when {
                result.isSuccess -> state.copy(isLoading = false, success = true)
                result.isFailure -> state.copy(isLoading = false, error = result.exceptionOrNull()?.message)
                else -> state.copy(isLoading = false, error = "Unknown error")
            }
        }
    }

    private fun validateHabit(state: CreateHabitUiState, userId: Int): String? {
        val titleError = Validators.validateHabitTitle(state.title)
        if (titleError != null) return "Invalid title"

        val descriptionError = Validators.validateHabitDescription(state.description)
        if (descriptionError != null) return "Description too long"

        if (state.disciplineId == null) return "Discipline is required"

        if (state.tasks.isEmpty()) return "At least one task is required"

        for ((index, task) in state.tasks.withIndex()) {
            val taskError = validateTask(task)
            if (taskError != null) return "Task ${index + 1}: $taskError"
        }

        if (userId < 1) return "Invalid user"

        return null
    }

    private fun validateTask(task: TaskUiState): String? {
        val titleError = Validators.validateTaskTitle(task.title)
        if (titleError != null) return "Invalid title"

        if (task.startDate.isEmpty()) return "Start date is required"

        if (task.periodLength < 1) return "Invalid period length"

        return null
    }

    private fun buildCreateHabitRequest(state: CreateHabitUiState, userId: Int): CreateHabitRequestDto {
        val tasks = state.tasks.map { task ->
            CreateHabitTaskRequestDto(
                title = task.title,
                description = task.description.ifEmpty { null },
                habitDisciplineId = task.habitDisciplineId,
                weekDays = task.weekDays.ifEmpty { null },
                difficulty = task.difficulty,
                frequency = task.frequency,
                periodLength = task.periodLength,
                periodUnit = task.periodUnit,
                startDate = task.startDate,
                completionCriteria = task.completionCriteria,
                evidence = task.evidence?.let { com.example.leveluplife.data.network.dto.TaskEvidence.valueOf(it) },
                repetitionCriteria = task.repetitionCriteria?.let { rc ->
                    RepetitionCriteriaRequestDto(
                        repetitions = rc.repetitions,
                        measurementUnit = MeasurementUnit.valueOf(rc.measurementUnit),
                        isPartialAllowed = rc.isPartialAllowed,
                        isActive = rc.isActive
                    )
                },
                xpValue = task.xpValue,
                isActive = task.isActive
            )
        }

        return CreateHabitRequestDto(
            title = state.title,
            description = state.description.ifEmpty { null },
            disciplineId = state.disciplineId!!,
            userId = userId,
            tasks = tasks
        )
    }

    fun reset() {
        _uiState.value = CreateHabitUiState()
    }
}
