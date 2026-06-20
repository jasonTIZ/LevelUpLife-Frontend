package com.example.leveluplife.ui.updatetask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.habits.HabitDisciplineRepository
import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.habits.HabitTaskConflictFailure
import com.example.leveluplife.data.habits.HabitTaskRepository
import com.example.leveluplife.data.habits.HabitTaskValidationFailure
import com.example.leveluplife.data.network.dto.HabitDisciplineDto
import com.example.leveluplife.domain.validation.HabitTaskValidationOptions
import com.example.leveluplife.domain.validation.HabitTaskValidators
import com.example.leveluplife.ui.createtask.HabitTaskFormHandlers
import com.example.leveluplife.ui.createtask.HabitTaskFormState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate

class UpdateHabitTaskViewModel(
    private val taskId: Int,
    private val habitRepository: HabitRepository,
    private val habitTaskRepository: HabitTaskRepository,
    private val disciplineRepository: HabitDisciplineRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UpdateHabitTaskUiState())
    val state: StateFlow<UpdateHabitTaskUiState> = _state.asStateFlow()

    init {
        loadDisciplines()
        loadTask()
    }

    private fun loadDisciplines() {
        _state.update { it.copy(isDisciplinesLoading = true) }
        viewModelScope.launch {
            disciplineRepository.getAll()
                .onSuccess { list ->
                    _state.update { current ->
                        syncDisciplineFilter(
                            current.copy(
                                allDisciplines = list.filter { it.isActive },
                                isDisciplinesLoading = false,
                            ),
                        )
                    }
                }
                .onFailure {
                    _state.update { it.copy(isDisciplinesLoading = false) }
                }
        }
    }

    fun loadTask() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, loadError = null, showConflictDialog = false) }

            val habitsResult = habitRepository.getActiveHabits(page = 1)
            val taskResult = habitTaskRepository.getHabitTask(taskId)

            val habits = habitsResult.getOrNull()?.habits.orEmpty()
            taskResult
                .onSuccess { task ->
                    _state.update { current ->
                        syncDisciplineFilter(
                            current.copy(
                                isLoading = false,
                                form = HabitTaskFormState.fromTask(task, habits),
                                originalStartDate = task.startDate.trim(),
                                selectedCategoryId = task.resolvedCategoryId ?: current.selectedCategoryId,
                            ),
                        )
                    }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            loadError = mapNetworkMessage(t),
                        )
                    }
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

    private fun syncDisciplineFilter(state: UpdateHabitTaskUiState): UpdateHabitTaskUiState {
        // If the task has no discipline of its own (created via the habit flow), default
        // to the parent habit's discipline, matched by name against the loaded list.
        // The list endpoint only returns the habit's discipline name, not its id.
        val habit = state.form.habits.firstOrNull { it.id == state.form.selectedHabitId }
        val disciplineId = state.form.selectedDisciplineId
            ?: habit?.disciplineName?.takeIf { it.isNotBlank() }?.let { name ->
                state.allDisciplines.firstOrNull { it.name.equals(name, ignoreCase = true) }?.id
            }

        val categoryFromDiscipline = disciplineId?.let { id ->
            state.allDisciplines.firstOrNull { it.id == id }?.categoryId
        }
        val categoryId = state.selectedCategoryId ?: categoryFromDiscipline
        val byCategory = filterDisciplines(state.allDisciplines, categoryId)
        // Always offer disciplines: if the task's category has none (or can't be
        // resolved), fall back to all active disciplines so the picker is never empty.
        val filtered = byCategory.ifEmpty { state.allDisciplines }
        // Clear only once disciplines have loaded and the id is genuinely absent —
        // otherwise the initial parallel load would wipe a valid selection.
        val resolvedDisciplineId = if (
            disciplineId != null &&
            state.allDisciplines.isNotEmpty() &&
            filtered.none { it.id == disciplineId }
        ) {
            null
        } else {
            disciplineId
        }

        return state.copy(
            selectedCategoryId = categoryId,
            disciplines = filtered,
            form = state.form.copy(selectedDisciplineId = resolvedDisciplineId),
        )
    }

    fun onDisciplineChange(disciplineId: Int) {
        _state.update { it.copy(form = HabitTaskFormHandlers.onDisciplineChange(it.form, disciplineId)) }
    }

    fun onTitleChange(value: String) {
        _state.update { it.copy(form = HabitTaskFormHandlers.onTitleChange(it.form, value)) }
    }

    fun onDescriptionChange(value: String) {
        _state.update { it.copy(form = HabitTaskFormHandlers.onDescriptionChange(it.form, value)) }
    }

    fun onDifficultyChange(value: String) {
        _state.update { it.copy(form = HabitTaskFormHandlers.onDifficultyChange(it.form, value)) }
    }

    fun onFrequencyChange(value: String) {
        _state.update { it.copy(form = HabitTaskFormHandlers.onFrequencyChange(it.form, value)) }
    }

    fun onPeriodLengthChange(value: String) {
        _state.update { it.copy(form = HabitTaskFormHandlers.onPeriodLengthChange(it.form, value)) }
    }

    fun onPeriodUnitChange(value: String) {
        _state.update { it.copy(form = HabitTaskFormHandlers.onPeriodUnitChange(it.form, value)) }
    }

    fun onStartDateChange(value: String) {
        _state.update { it.copy(form = HabitTaskFormHandlers.onStartDateChange(it.form, value)) }
    }

    fun onCompletionCriteriaChange(value: String) {
        _state.update { it.copy(form = HabitTaskFormHandlers.onCompletionCriteriaChange(it.form, value)) }
    }

    fun onRepetitionsChange(value: String) {
        _state.update { it.copy(form = HabitTaskFormHandlers.onRepetitionsChange(it.form, value)) }
    }

    fun onMeasurementUnitChange(value: String) {
        _state.update { it.copy(form = HabitTaskFormHandlers.onMeasurementUnitChange(it.form, value)) }
    }

    fun onEvidenceChange(value: String) {
        _state.update { it.copy(form = HabitTaskFormHandlers.onEvidenceChange(it.form, value)) }
    }

    fun onPartialAllowedChange(value: Boolean) {
        _state.update { it.copy(form = it.form.copy(isPartialAllowed = value)) }
    }

    fun onTimerSecondsDefinedChange(value: String) {
        _state.update { it.copy(form = HabitTaskFormHandlers.onTimerSecondsDefinedChange(it.form, value)) }
    }

    fun onTimerSecondsLongChange(value: String) {
        _state.update { it.copy(form = HabitTaskFormHandlers.onTimerSecondsLongChange(it.form, value)) }
    }

    fun onTimerPauseAllowedChange(value: Boolean) {
        _state.update { it.copy(form = HabitTaskFormHandlers.onTimerPauseAllowedChange(it.form, value)) }
    }

    fun dismissSubmitError() {
        _state.update { it.copy(form = it.form.copy(submitError = null)) }
    }

    fun dismissConflictDialog() {
        _state.update { it.copy(showConflictDialog = false) }
    }

    fun consumeUpdatedTask() {
        _state.update { it.copy(updatedTask = null) }
    }

    fun submit() {
        val sanitizedForm = _state.value.form.sanitizedForValidation()
        if (sanitizedForm.selectedDisciplineId == null) {
            _state.update {
                it.copy(
                    form = sanitizedForm.copy(
                        submitError = "Selecciona una disciplina",
                        showValidationErrors = true,
                    ),
                )
            }
            return
        }

        val validationOptions = HabitTaskValidationOptions(
            today = LocalDate.now(),
            preservedStartDate = _state.value.originalStartDate,
        )
        val errors = HabitTaskValidators.validateForm(
            sanitizedForm.toFormInput(),
            validationOptions,
        )
        if (errors.hasErrors) {
            _state.update {
                it.copy(
                    form = sanitizedForm.copy(
                        fieldErrors = errors,
                        showValidationErrors = true,
                        submitError = null,
                    ),
                )
            }
            return
        }

        _state.update { it.copy(form = sanitizedForm) }

        val request = _state.value.form.toRequest()
        if (request == null) {
            _state.update {
                it.copy(
                    form = it.form.copy(
                        showValidationErrors = true,
                        submitError = "No se pudo enviar la tarea. Revisá que todos los campos estén completos.",
                    ),
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, form = it.form.copy(submitError = null)) }
            habitTaskRepository.updateHabitTask(taskId, request)
                .onSuccess { task ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            updatedTask = task,
                            originalStartDate = task.startDate.trim(),
                            form = HabitTaskFormState.fromTask(task, it.form.habits),
                        ).let { updated ->
                            syncDisciplineFilter(updated)
                        }
                    }
                }
                .onFailure { t ->
                    when (t) {
                        is HabitTaskValidationFailure -> _state.update {
                            it.copy(
                                isSubmitting = false,
                                form = it.form.copy(
                                    fieldErrors = t.fieldErrors,
                                    showValidationErrors = true,
                                    submitError = null,
                                ),
                            )
                        }
                        is HabitTaskConflictFailure -> _state.update {
                            it.copy(
                                isSubmitting = false,
                                showConflictDialog = true,
                                form = it.form.copy(submitError = null),
                            )
                        }
                        else -> _state.update {
                            it.copy(
                                isSubmitting = false,
                                form = it.form.copy(submitError = mapSubmitError(t)),
                            )
                        }
                    }
                }
        }
    }

    private fun mapNetworkMessage(t: Throwable): String = when (t) {
        is IOException -> "Sin conexión. Revisa tu red e intenta de nuevo."
        else -> t.message ?: "Error desconocido"
    }

    private fun mapSubmitError(t: Throwable): String = when (t) {
        is IOException -> "Sin conexión. Revisa tu red e intenta de nuevo."
        else -> t.message ?: "No se pudo actualizar la tarea."
    }

    class Factory(
        private val taskId: Int,
        private val habitRepository: HabitRepository,
        private val habitTaskRepository: HabitTaskRepository,
        private val disciplineRepository: HabitDisciplineRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(UpdateHabitTaskViewModel::class.java))
            return UpdateHabitTaskViewModel(
                taskId,
                habitRepository,
                habitTaskRepository,
                disciplineRepository,
            ) as T
        }
    }
}
