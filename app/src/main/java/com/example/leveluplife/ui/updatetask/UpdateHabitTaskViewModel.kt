package com.example.leveluplife.ui.updatetask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.habits.HabitTaskConflictFailure
import com.example.leveluplife.data.habits.HabitTaskRepository
import com.example.leveluplife.data.habits.HabitTaskValidationFailure
import com.example.leveluplife.domain.validation.HabitTaskValidators
import com.example.leveluplife.ui.createtask.HabitTaskFormHandlers
import com.example.leveluplife.ui.createtask.HabitTaskFormState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class UpdateHabitTaskViewModel(
    private val taskId: Int,
    private val habitRepository: HabitRepository,
    private val habitTaskRepository: HabitTaskRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UpdateHabitTaskUiState())
    val state: StateFlow<UpdateHabitTaskUiState> = _state.asStateFlow()

    init {
        loadTask()
    }

    fun loadTask() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, loadError = null, showConflictDialog = false) }

            val habitsResult = habitRepository.getActiveHabits(page = 1)
            val taskResult = habitTaskRepository.getHabitTask(taskId)

            val habits = habitsResult.getOrNull()?.habits.orEmpty()
            taskResult
                .onSuccess { task ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            form = HabitTaskFormState.fromTask(task, habits),
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
        val errors = HabitTaskValidators.validateForm(sanitizedForm.toFormInput())
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
                    _state.update { it.copy(isSubmitting = false, updatedTask = task) }
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
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(UpdateHabitTaskViewModel::class.java))
            return UpdateHabitTaskViewModel(taskId, habitRepository, habitTaskRepository) as T
        }
    }
}
