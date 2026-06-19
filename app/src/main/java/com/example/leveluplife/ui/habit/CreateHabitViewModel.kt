package com.example.leveluplife.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.habits.HabitDisciplineRepository
import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.network.dto.CreateHabitRequestDto
import com.example.leveluplife.data.network.dto.CreateHabitTaskRequestDto
import com.example.leveluplife.data.network.dto.MeasurementUnit
import com.example.leveluplife.data.network.dto.RepetitionCriteriaRequestDto
import com.example.leveluplife.data.network.dto.TimerCriteriaRequestDto
import com.example.leveluplife.domain.validation.HabitTaskValidators
import com.example.leveluplife.data.network.dto.TaskCompletionCriteria
import com.example.leveluplife.data.network.dto.TaskDifficulty
import com.example.leveluplife.data.network.dto.TaskEvidence
import com.example.leveluplife.data.network.dto.TaskFrequency
import com.example.leveluplife.data.network.dto.TaskPeriodUnit
import com.example.leveluplife.domain.validation.Validators
import com.example.leveluplife.ui.createtask.HabitTaskFormHandlers
import com.example.leveluplife.ui.createtask.HabitTaskFormState
import com.example.leveluplife.ui.createtask.TaskFormTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateHabitViewModel(
    private val repository: HabitRepository,
    private val disciplineRepository: HabitDisciplineRepository,
) : ViewModel() {

    companion object {
        fun Factory(
            repository: HabitRepository,
            disciplineRepository: HabitDisciplineRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CreateHabitViewModel(repository, disciplineRepository) as T
            }
        }
    }

    private val _uiState = MutableStateFlow(CreateHabitUiState())
    val uiState: StateFlow<CreateHabitUiState> = _uiState.asStateFlow()

    init {
        loadDisciplines()
    }

    private fun loadDisciplines() {
        _uiState.value = _uiState.value.copy(isDisciplinesLoading = true)
        viewModelScope.launch {
            disciplineRepository.getAll()
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(
                        disciplines = list.filter { it.isActive },
                        isDisciplinesLoading = false,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isDisciplinesLoading = false)
                }
        }
    }

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
        _uiState.value = _uiState.value.copy(tasks = _uiState.value.tasks + HabitTaskFormState())
    }

    fun removeTask(index: Int) {
        if (_uiState.value.tasks.size > 1) {
            _uiState.value = _uiState.value.copy(
                tasks = _uiState.value.tasks.toMutableList().apply { removeAt(index) },
            )
        }
    }

    private fun updateTaskForm(index: Int, update: (HabitTaskFormState) -> HabitTaskFormState) {
        val tasks = _uiState.value.tasks.toMutableList()
        tasks[index] = update(tasks[index])
        _uiState.value = _uiState.value.copy(tasks = tasks)
    }

    fun onTaskTitleChange(index: Int, value: String) =
        updateTaskForm(index) { HabitTaskFormHandlers.onTitleChange(it, value) }

    fun onTaskDescriptionChange(index: Int, value: String) =
        updateTaskForm(index) { HabitTaskFormHandlers.onDescriptionChange(it, value) }

    fun onTaskDifficultyChange(index: Int, value: String) =
        updateTaskForm(index) { HabitTaskFormHandlers.onDifficultyChange(it, value) }

    fun onTaskFrequencyChange(index: Int, value: String) =
        updateTaskForm(index) { HabitTaskFormHandlers.onFrequencyChange(it, value) }

    fun onTaskPeriodLengthChange(index: Int, value: String) =
        updateTaskForm(index) { HabitTaskFormHandlers.onPeriodLengthChange(it, value) }

    fun onTaskPeriodUnitChange(index: Int, value: String) =
        updateTaskForm(index) { HabitTaskFormHandlers.onPeriodUnitChange(it, value) }

    fun onTaskStartDateChange(index: Int, value: String) =
        updateTaskForm(index) { HabitTaskFormHandlers.onStartDateChange(it, value) }

    fun onTaskCompletionCriteriaChange(index: Int, value: String) =
        updateTaskForm(index) { HabitTaskFormHandlers.onCompletionCriteriaChange(it, value) }

    fun onTaskRepetitionsChange(index: Int, value: String) =
        updateTaskForm(index) { HabitTaskFormHandlers.onRepetitionsChange(it, value) }

    fun onTaskMeasurementUnitChange(index: Int, value: String) =
        updateTaskForm(index) { HabitTaskFormHandlers.onMeasurementUnitChange(it, value) }

    fun onTaskEvidenceChange(index: Int, value: String) =
        updateTaskForm(index) { HabitTaskFormHandlers.onEvidenceChange(it, value) }

    fun onTaskPartialAllowedChange(index: Int, value: Boolean) =
        updateTaskForm(index) { it.copy(isPartialAllowed = value) }

    fun onTaskTimerSecondsDefinedChange(index: Int, value: String) =
        updateTaskForm(index) { HabitTaskFormHandlers.onTimerSecondsDefinedChange(it, value) }

    fun onTaskTimerSecondsLongChange(index: Int, value: String) =
        updateTaskForm(index) { HabitTaskFormHandlers.onTimerSecondsLongChange(it, value) }

    fun onTaskTimerPauseAllowedChange(index: Int, value: Boolean) =
        updateTaskForm(index) { HabitTaskFormHandlers.onTimerPauseAllowedChange(it, value) }

    fun applyTaskTemplate(index: Int, template: TaskFormTemplate) =
        updateTaskForm(index) { HabitTaskFormState.applyTemplate(it, template) }

    fun dismissTaskError(index: Int) =
        updateTaskForm(index) { it.copy(submitError = null) }

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
        if (Validators.validateHabitTitle(state.title) != null) return "Título inválido"
        if (Validators.validateHabitDescription(state.description) != null) return "Descripción muy larga"
        if (state.disciplineId == null) return "Selecciona una disciplina"
        if (state.tasks.isEmpty()) return "Agrega al menos una tarea"
        for ((index, task) in state.tasks.withIndex()) {
            val taskError = validateTask(task)
            if (taskError != null) return "Tarea ${index + 1}: $taskError"
        }
        if (userId < 1) return "Usuario inválido"
        return null
    }

    private fun validateTask(task: HabitTaskFormState): String? {
        if (task.title.isBlank()) return "El título es requerido"
        if (task.startDate.isBlank()) return "La fecha de inicio es requerida"
        val periodLength = task.periodLength.toIntOrNull()
        if (periodLength == null || periodLength < 1) return "Período inválido"
        if (task.completionCriteria == "REPETITIONS") {
            val reps = task.repetitions.toIntOrNull()
            if (reps == null || reps < 1) return "Repeticiones inválidas"
        }
        if (task.completionCriteria == "EVIDENCE" && task.evidence.isNullOrBlank()) {
            return "Tipo de evidencia requerido"
        }
        if (task.completionCriteria == "TIMER") {
            val seconds = task.timerSecondsDefined.toIntOrNull()
            if (seconds == null || seconds < 1 || seconds > HabitTaskValidators.TIMER_SECONDS_MAX) {
                return "Duración inválida (1 a ${HabitTaskValidators.TIMER_SECONDS_MAX} segundos)"
            }
        }
        return null
    }

    private fun buildCreateHabitRequest(state: CreateHabitUiState, userId: Int): CreateHabitRequestDto {
        val tasks = state.tasks.map { task ->
            val criteria = TaskCompletionCriteria.valueOf(task.completionCriteria ?: "REPETITIONS")
            CreateHabitTaskRequestDto(
                title = task.title.trim(),
                description = task.description.trim().ifBlank { null },
                habitDisciplineId = null,
                weekDays = null,
                difficulty = TaskDifficulty.valueOf(task.difficulty ?: "MEDIUM"),
                frequency = TaskFrequency.valueOf(task.frequency ?: "WEEKLY"),
                periodLength = task.periodLength.toIntOrNull() ?: 1,
                periodUnit = TaskPeriodUnit.valueOf(task.periodUnit ?: "WEEKS"),
                startDate = task.startDate.trim(),
                completionCriteria = criteria,
                evidence = if (criteria == TaskCompletionCriteria.EVIDENCE) {
                    task.evidence?.let { TaskEvidence.valueOf(it) }
                } else null,
                repetitionCriteria = if (criteria == TaskCompletionCriteria.REPETITIONS) {
                    RepetitionCriteriaRequestDto(
                        repetitions = task.repetitions.toIntOrNull() ?: 1,
                        measurementUnit = MeasurementUnit.valueOf(task.measurementUnit ?: "SERIES"),
                        isPartialAllowed = task.isPartialAllowed,
                        isActive = true,
                    )
                } else null,
                timerCriteria = if (criteria == TaskCompletionCriteria.TIMER) {
                    TimerCriteriaRequestDto(
                        numSecondsDefined = task.timerSecondsDefined.toIntOrNull() ?: 1,
                        numSecondsLong = task.timerSecondsLong.toIntOrNull(),
                        typePauseIsAllowed = task.timerPauseAllowed,
                        statusTimerCriteriaIsActive = true,
                    )
                } else null,
                xpValue = null,
                isActive = true,
            )
        }

        return CreateHabitRequestDto(
            title = state.title,
            description = state.description.ifEmpty { null },
            disciplineId = state.disciplineId!!,
            userId = userId,
            tasks = tasks,
        )
    }

    fun reset() {
        _uiState.value = CreateHabitUiState()
    }
}
