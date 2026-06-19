package com.example.leveluplife.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.categories.HabitCategoryRepository
import com.example.leveluplife.data.habits.HabitDisciplineRepository
import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.network.dto.CreateHabitRequestDto
import com.example.leveluplife.data.network.dto.CreateHabitTaskRequestDto
import com.example.leveluplife.data.network.dto.HabitCategoryDto
import com.example.leveluplife.data.network.dto.HabitDisciplineDto
import com.example.leveluplife.data.network.dto.MeasurementUnit
import com.example.leveluplife.data.network.dto.RepetitionCriteriaRequestDto
import com.example.leveluplife.data.network.dto.TaskCompletionCriteria
import com.example.leveluplife.data.network.dto.TaskDifficulty
import com.example.leveluplife.data.network.dto.TaskEvidence
import com.example.leveluplife.data.network.dto.TaskFrequency
import com.example.leveluplife.data.network.dto.TaskPeriodUnit
import com.example.leveluplife.domain.validation.FieldError
import com.example.leveluplife.domain.validation.HabitTaskFieldError
import com.example.leveluplife.domain.validation.HabitTaskFormErrors
import com.example.leveluplife.domain.validation.HabitTaskValidationOptions
import com.example.leveluplife.domain.validation.HabitTaskValidators
import com.example.leveluplife.domain.validation.Validators
import com.example.leveluplife.ui.createtask.HabitTaskFormHandlers
import com.example.leveluplife.ui.createtask.HabitTaskFormState
import com.example.leveluplife.ui.createtask.TaskFormTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class CreateHabitViewModel(
    private val repository: HabitRepository,
    private val disciplineRepository: HabitDisciplineRepository,
    private val categoryRepository: HabitCategoryRepository,
) : ViewModel() {

    companion object {
        private const val CATEGORY_PAGE_SIZE = 100

        fun Factory(
            repository: HabitRepository,
            disciplineRepository: HabitDisciplineRepository,
            categoryRepository: HabitCategoryRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CreateHabitViewModel(repository, disciplineRepository, categoryRepository) as T
            }
        }
    }

    private val _uiState = MutableStateFlow(CreateHabitUiState())
    val uiState: StateFlow<CreateHabitUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadDisciplines()
    }

    private fun loadCategories() {
        _uiState.value = _uiState.value.copy(isCategoriesLoading = true)
        viewModelScope.launch {
            val allCategories = mutableListOf<HabitCategoryDto>()
            var page = 1
            var hasMore = true

            while (hasMore) {
                val result = categoryRepository.getActiveCategories(page = page, pageSize = CATEGORY_PAGE_SIZE)
                val response = result.getOrNull()
                if (response == null) {
                    _uiState.value = _uiState.value.copy(isCategoriesLoading = false)
                    return@launch
                }

                allCategories.addAll(response.categories.orEmpty().filter { it.isActive })
                val pagination = response.pagination
                val currentPage = pagination?.currentPage ?: page
                val totalPages = pagination?.totalPages ?: currentPage
                hasMore = currentPage < totalPages
                page = currentPage + 1
            }

            val activeCategories = allCategories.distinctBy { it.id }
            val firstCategoryId = activeCategories.firstOrNull()?.id
            val currentState = _uiState.value
            val filtered = filterDisciplines(currentState.allDisciplines, firstCategoryId)

            _uiState.value = currentState.copy(
                categories = activeCategories,
                selectedCategoryId = firstCategoryId,
                isCategoriesLoading = false,
                disciplines = filtered,
                tasks = clearInvalidTaskDisciplines(currentState.tasks, filtered),
            )
        }
    }

    private fun loadDisciplines() {
        _uiState.value = _uiState.value.copy(isDisciplinesLoading = true)
        viewModelScope.launch {
            disciplineRepository.getAll()
                .onSuccess { list ->
                    val activeDisciplines = list.filter { it.isActive }
                    val currentState = _uiState.value
                    val filtered = filterDisciplines(activeDisciplines, currentState.selectedCategoryId)
                    _uiState.value = currentState.copy(
                        allDisciplines = activeDisciplines,
                        disciplines = filtered,
                        isDisciplinesLoading = false,
                        tasks = clearInvalidTaskDisciplines(currentState.tasks, filtered),
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isDisciplinesLoading = false)
                }
        }
    }

    private fun filterDisciplines(
        allDisciplines: List<HabitDisciplineDto>,
        categoryId: Int?,
    ): List<HabitDisciplineDto> {
        if (categoryId == null) return emptyList()
        return allDisciplines.filter { it.categoryId == categoryId && it.isActive }
    }

    private fun clearInvalidTaskDisciplines(
        tasks: List<HabitTaskFormState>,
        filteredDisciplines: List<HabitDisciplineDto>,
    ): List<HabitTaskFormState> = tasks.map { task ->
        if (task.selectedDisciplineId != null &&
            filteredDisciplines.none { it.id == task.selectedDisciplineId }
        ) {
            task.copy(selectedDisciplineId = null)
        } else {
            task
        }
    }

    fun setCategoryId(categoryId: Int) {
        val currentState = _uiState.value
        val filtered = filterDisciplines(currentState.allDisciplines, categoryId)

        _uiState.value = currentState.copy(
            selectedCategoryId = categoryId,
            disciplines = filtered,
            tasks = clearInvalidTaskDisciplines(currentState.tasks, filtered),
        )
    }

    fun setTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title, habitTitleError = null, error = null)
    }

    fun setDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description, error = null)
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

    fun onTaskDisciplineChange(index: Int, disciplineId: Int) =
        updateTaskForm(index) { HabitTaskFormHandlers.onDisciplineChange(it, disciplineId) }

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

    fun applyTaskTemplate(index: Int, template: TaskFormTemplate) =
        updateTaskForm(index) { HabitTaskFormState.applyTemplate(it, template) }

    fun dismissTaskError(index: Int) =
        updateTaskForm(index) { it.copy(submitError = null) }

    fun createHabit() {
        val state = _uiState.value
        val userId = repository.getCurrentUserId()
        val validation = validateHabit(state, userId)
        if (validation.error != null) {
            _uiState.value = state.copy(
                tasks = validation.tasks,
                habitTitleError = validation.habitTitleError,
                error = validation.error,
            )
            return
        }

        _uiState.value = state.copy(
            tasks = validation.tasks,
            isLoading = true,
            error = null,
            habitTitleError = null,
        )

        viewModelScope.launch {
            val request = buildCreateHabitRequest(_uiState.value, userId)
            val result = repository.createHabit(request)

            _uiState.value = when {
                result.isSuccess -> _uiState.value.copy(isLoading = false, success = true)
                result.isFailure -> _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message,
                )
                else -> _uiState.value.copy(isLoading = false, error = "Unknown error")
            }
        }
    }

    private data class HabitValidationResult(
        val tasks: List<HabitTaskFormState>,
        val habitTitleError: String?,
        val error: String?,
    )

    private fun validateHabit(state: CreateHabitUiState, userId: Int): HabitValidationResult {
        val habitTitleError = mapHabitTitleError(Validators.validateHabitTitle(state.title.trim()))
        val updatedTasks = state.tasks.map(::validateTaskFormState)
        val taskSummary = updatedTasks.withIndex().firstNotNullOfOrNull { (index, task) ->
            taskValidationSummary(task, index)
        }

        val error = when {
            habitTitleError != null -> habitTitleError
            Validators.validateHabitDescription(state.description) != null -> "Descripción muy larga"
            state.tasks.isEmpty() -> "Agrega al menos una tarea"
            taskSummary != null -> taskSummary
            userId < 1 -> "Usuario inválido"
            else -> null
        }

        return HabitValidationResult(
            tasks = updatedTasks,
            habitTitleError = habitTitleError,
            error = error,
        )
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

    private fun buildCreateHabitRequest(state: CreateHabitUiState, userId: Int): CreateHabitRequestDto {
        val tasks = state.tasks.map { task ->
            val criteria = TaskCompletionCriteria.valueOf(task.completionCriteria ?: "REPETITIONS")
            CreateHabitTaskRequestDto(
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
                xpValue = null,
                isActive = true,
            )
        }

        return CreateHabitRequestDto(
            title = state.title,
            description = state.description.ifEmpty { null },
            disciplineId = state.tasks.first().selectedDisciplineId!!,
            userId = userId,
            tasks = tasks,
        )
    }

    fun reset() {
        _uiState.value = CreateHabitUiState()
    }
}
