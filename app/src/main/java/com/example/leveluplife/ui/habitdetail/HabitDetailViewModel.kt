package com.example.leveluplife.ui.habitdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.habits.HabitDisciplineRepository
import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.network.dto.CreateHabitTaskRequestDto
import com.example.leveluplife.data.network.dto.HabitDto
import com.example.leveluplife.data.network.dto.MeasurementUnit
import com.example.leveluplife.data.network.dto.CreateTimerCriteriaRequest
import com.example.leveluplife.data.network.dto.RepetitionCriteriaRequestDto
import com.example.leveluplife.data.network.dto.TaskCompletionCriteria
import com.example.leveluplife.data.network.dto.TaskDifficulty
import com.example.leveluplife.data.network.dto.TaskEvidence
import com.example.leveluplife.data.network.dto.TaskFrequency
import com.example.leveluplife.data.network.dto.TaskPeriodUnit
import com.example.leveluplife.data.network.dto.UpdateHabitRequestDto
import com.example.leveluplife.domain.validation.FieldError
import com.example.leveluplife.domain.validation.HabitTaskFieldError
import com.example.leveluplife.domain.validation.HabitTaskFormErrors
import com.example.leveluplife.domain.validation.HabitTaskValidationOptions
import com.example.leveluplife.domain.validation.HabitTaskValidators
import com.example.leveluplife.domain.validation.Validators
import com.example.leveluplife.ui.createtask.HabitTaskFormHandlers
import com.example.leveluplife.ui.createtask.HabitTaskFormState
import com.example.leveluplife.ui.createtask.TaskFormTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class HabitDetailViewModel(
    private val habitRepository: HabitRepository,
    private val disciplineRepository: HabitDisciplineRepository,
    private val habitId: Int,
) : ViewModel() {

    private val _state = MutableStateFlow(HabitDetailUiState())
    val state: StateFlow<HabitDetailUiState> = _state.asStateFlow()

    private var allDisciplines: List<com.example.leveluplife.data.network.dto.HabitDisciplineDto> = emptyList()

    init {
        loadHabit()
    }

    fun loadHabit() {
        viewModelScope.launch {
            refreshHabit()
        }
    }

    suspend fun refreshHabit() {
        _state.update { it.copy(isLoading = true, error = null) }
        val result = withContext(Dispatchers.IO) {
            habitRepository.getHabitById(habitId)
        }
        result
            .onSuccess { habit ->
                _state.update { it.copy(isLoading = false, habit = habit) }
            }
            .onFailure { t ->
                _state.update {
                    it.copy(isLoading = false, error = t.message ?: "Error desconocido")
                }
            }
    }

    fun startEditing() {
        val habit = _state.value.habit ?: return
        _state.update {
            it.copy(
                isEditing = true,
                editTitle = habit.title,
                editDescription = habit.description,
                editTitleError = null,
                newTasks = emptyList(),
                saveError = null,
            )
        }
        loadDisciplines(habit)
    }

    fun cancelEditing() {
        _state.update {
            it.copy(
                isEditing = false,
                editTitleError = null,
                newTasks = emptyList(),
                saveError = null,
            )
        }
    }

    fun setEditTitle(title: String) {
        _state.update { it.copy(editTitle = title, editTitleError = null, saveError = null) }
    }

    fun setEditDescription(description: String) {
        _state.update { it.copy(editDescription = description, saveError = null) }
    }

    private fun loadDisciplines(habit: HabitDto) {
        val categoryId = habit.categoryId.takeIf { it > 0 }
            ?: habit.tasks.firstNotNullOfOrNull { it.resolvedCategoryId }
        if (categoryId == null) return
        _state.update { it.copy(isDisciplinesLoading = true) }
        viewModelScope.launch {
            disciplineRepository.getAll()
                .onSuccess { list ->
                    allDisciplines = list.filter { it.isActive }
                    val filtered = allDisciplines.filter { it.categoryId == categoryId }
                    _state.update { it.copy(disciplines = filtered, isDisciplinesLoading = false) }
                }
                .onFailure {
                    _state.update { it.copy(isDisciplinesLoading = false) }
                }
        }
    }

    fun addNewTask() {
        _state.update { it.copy(newTasks = it.newTasks + HabitTaskFormState()) }
    }

    fun removeNewTask(index: Int) {
        _state.update {
            it.copy(newTasks = it.newTasks.toMutableList().apply { removeAt(index) })
        }
    }

    private fun updateNewTask(index: Int, update: (HabitTaskFormState) -> HabitTaskFormState) {
        val tasks = _state.value.newTasks.toMutableList()
        tasks[index] = update(tasks[index])
        _state.update { it.copy(newTasks = tasks) }
    }

    fun onTaskDisciplineChange(index: Int, disciplineId: Int) =
        updateNewTask(index) { HabitTaskFormHandlers.onDisciplineChange(it, disciplineId) }

    fun onTaskTitleChange(index: Int, value: String) =
        updateNewTask(index) { HabitTaskFormHandlers.onTitleChange(it, value) }

    fun onTaskDescriptionChange(index: Int, value: String) =
        updateNewTask(index) { HabitTaskFormHandlers.onDescriptionChange(it, value) }

    fun onTaskDifficultyChange(index: Int, value: String) =
        updateNewTask(index) { HabitTaskFormHandlers.onDifficultyChange(it, value) }

    fun onTaskFrequencyChange(index: Int, value: String) =
        updateNewTask(index) { HabitTaskFormHandlers.onFrequencyChange(it, value) }

    fun onTaskPeriodLengthChange(index: Int, value: String) =
        updateNewTask(index) { HabitTaskFormHandlers.onPeriodLengthChange(it, value) }

    fun onTaskPeriodUnitChange(index: Int, value: String) =
        updateNewTask(index) { HabitTaskFormHandlers.onPeriodUnitChange(it, value) }

    fun onTaskStartDateChange(index: Int, value: String) =
        updateNewTask(index) { HabitTaskFormHandlers.onStartDateChange(it, value) }

    fun onTaskCompletionCriteriaChange(index: Int, value: String) =
        updateNewTask(index) { HabitTaskFormHandlers.onCompletionCriteriaChange(it, value) }

    fun onTaskRepetitionsChange(index: Int, value: String) =
        updateNewTask(index) { HabitTaskFormHandlers.onRepetitionsChange(it, value) }

    fun onTaskMeasurementUnitChange(index: Int, value: String) =
        updateNewTask(index) { HabitTaskFormHandlers.onMeasurementUnitChange(it, value) }

    fun onTaskEvidenceChange(index: Int, value: String) =
        updateNewTask(index) { HabitTaskFormHandlers.onEvidenceChange(it, value) }

    fun onTaskPartialAllowedChange(index: Int, value: Boolean) =
        updateNewTask(index) { it.copy(isPartialAllowed = value) }

    fun onTaskTimerSecondsDefinedChange(index: Int, value: String) =
        updateNewTask(index) { HabitTaskFormHandlers.onTimerSecondsDefinedChange(it, value) }

    fun onTaskTimerSecondsLongChange(index: Int, value: String) =
        updateNewTask(index) { HabitTaskFormHandlers.onTimerSecondsLongChange(it, value) }

    fun onTaskTimerPauseAllowedChange(index: Int, value: Boolean) =
        updateNewTask(index) { HabitTaskFormHandlers.onTimerPauseAllowedChange(it, value) }

    fun applyTaskTemplate(index: Int, template: TaskFormTemplate) =
        updateNewTask(index) { HabitTaskFormState.applyTemplate(it, template) }

    fun dismissTaskError(index: Int) =
        updateNewTask(index) { it.copy(submitError = null) }

    fun onUpdateMessageShown() {
        _state.update { it.copy(updateSuccess = false) }
    }

    fun onAiDifficultyAlertShown() {
        _state.update { it.copy(aiDifficultyFailed = false) }
    }

    fun saveEdits() {
        val state = _state.value
        val habit = state.habit ?: return
        val userId = habitRepository.getCurrentUserId()

        val titleError = mapHabitTitleError(Validators.validateHabitTitle(state.editTitle.trim()))
        val validatedTasks = state.newTasks.map(::validateTaskFormState)
        val taskSummary = validatedTasks.withIndex().firstNotNullOfOrNull { (index, task) ->
            taskValidationSummary(task, index)
        }

        val error = when {
            titleError != null -> titleError
            Validators.validateHabitDescription(state.editDescription) != null -> "Descripción muy larga"
            taskSummary != null -> taskSummary
            userId < 1 -> "Usuario inválido"
            else -> null
        }

        if (error != null) {
            _state.update {
                it.copy(newTasks = validatedTasks, editTitleError = titleError, saveError = error)
            }
            return
        }

        _state.update {
            it.copy(newTasks = validatedTasks, isSaving = true, saveError = null, editTitleError = null)
        }

        viewModelScope.launch {
            val request = UpdateHabitRequestDto(
                id = habit.id,
                title = state.editTitle.trim(),
                description = state.editDescription.trim(),
                userId = userId,
                newTasks = validatedTasks.map(::buildTaskRequest),
            )
            val result = withContext(Dispatchers.IO) { habitRepository.updateHabit(request) }
            result
                .onSuccess { response ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            isEditing = false,
                            newTasks = emptyList(),
                            updateSuccess = true,
                            aiDifficultyFailed = response.aiDifficultyFailed,
                        )
                    }
                    refreshHabit()
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(isSaving = false, saveError = t.message ?: "No se pudo actualizar el hábito")
                    }
                }
        }
    }

    private fun validateTaskFormState(task: HabitTaskFormState): HabitTaskFormState {
        val sanitized = task.sanitizedForValidation()
        if (sanitized.selectedDisciplineId == null) {
            return sanitized.copy(
                showValidationErrors = true,
                submitError = "Selecciona una disciplina",
            )
        }

        val fieldErrors = HabitTaskValidators.validateForm(
            sanitized.toFormInput().copy(habitId = 1),
            HabitTaskValidationOptions(today = LocalDate.now()),
        ).copy(habitId = null)

        return if (fieldErrors.hasErrors) {
            sanitized.copy(
                fieldErrors = fieldErrors,
                showValidationErrors = true,
                submitError = null,
            )
        } else {
            sanitized.copy(
                fieldErrors = HabitTaskFormErrors(),
                showValidationErrors = false,
                submitError = null,
            )
        }
    }

    private fun taskValidationSummary(task: HabitTaskFormState, index: Int): String? {
        task.submitError?.let { return "Tarea ${index + 1}: $it" }
        if (!task.fieldErrors.hasErrors) return null

        return when {
            task.fieldErrors.title is HabitTaskFieldError.TooShort ->
                "Tarea ${index + 1}: el título debe tener al menos ${Validators.TASK_TITLE_MIN} caracteres"
            task.fieldErrors.title != null -> "Tarea ${index + 1}: revisa el título"
            task.fieldErrors.startDate != null -> "Tarea ${index + 1}: revisa la fecha de inicio"
            task.fieldErrors.timerSeconds != null ->
                "Tarea ${index + 1}: duración inválida (1 a ${HabitTaskValidators.TIMER_SECONDS_MAX} segundos)"
            task.fieldErrors.timerLong != null ->
                "Tarea ${index + 1}: el umbral largo debe superar la duración base"
            else -> "Tarea ${index + 1}: revisa los campos marcados"
        }
    }

    private fun mapHabitTitleError(error: FieldError?): String? = when (error) {
        null -> null
        FieldError.Required -> "El título del hábito es obligatorio"
        is FieldError.TooShort -> "El título del hábito debe tener al menos ${Validators.HABIT_TITLE_MIN} caracteres"
        is FieldError.TooLong -> "El título del hábito es demasiado largo"
        else -> "Título del hábito inválido"
    }

    private fun buildTaskRequest(task: HabitTaskFormState): CreateHabitTaskRequestDto {
        val criteria = TaskCompletionCriteria.valueOf(task.completionCriteria ?: "REPETITIONS")
        return CreateHabitTaskRequestDto(
            title = task.title.trim(),
            description = task.description.trim().ifBlank { null },
            habitDisciplineId = task.selectedDisciplineId,
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
                CreateTimerCriteriaRequest(
                    numSecondsDefined = task.timerSecondsDefined.trim().toInt(),
                    numSecondsLong = task.timerSecondsLong.trim().toIntOrNull(),
                    typePauseIsAllowed = task.timerPauseAllowed,
                    statusTimerCriteriaIsActive = true,
                )
            } else null,
            xpValue = null,
            isActive = true,
        )
    }

    class Factory(
        private val habitRepository: HabitRepository,
        private val disciplineRepository: HabitDisciplineRepository,
        private val habitId: Int,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(HabitDetailViewModel::class.java))
            return HabitDetailViewModel(habitRepository, disciplineRepository, habitId) as T
        }
    }
}
