package com.example.leveluplife.ui.createtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.habits.HabitTaskRepository
import com.example.leveluplife.data.habits.HabitTaskValidationFailure
import com.example.leveluplife.domain.validation.HabitTaskValidators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate

class CreateHabitTaskViewModel(
    private val habitRepository: HabitRepository,
    private val habitTaskRepository: HabitTaskRepository,
    preselectedHabitId: Int?,
) : ViewModel() {

    private val _state = MutableStateFlow(
        CreateHabitTaskUiState(
            form = HabitTaskFormState(
                selectedHabitId = preselectedHabitId?.takeIf { it > 0 },
                startDate = LocalDate.now().toString(),
            ),
        ),
    )
    val state: StateFlow<CreateHabitTaskUiState> = _state.asStateFlow()

    init {
        loadHabits()
    }

    fun loadHabits() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingHabits = true, habitsLoadError = null) }
            habitRepository.getActiveHabits(page = 1)
                .onSuccess { response ->
                    val habits = response.habits.orEmpty()
                    _state.update { current ->
                        val selected = current.form.selectedHabitId ?: habits.firstOrNull()?.id
                        current.copy(
                            isLoadingHabits = false,
                            form = current.form.copy(
                                habits = habits,
                                selectedHabitId = selected,
                            ),
                        )
                    }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(
                            isLoadingHabits = false,
                            habitsLoadError = mapNetworkMessage(t),
                        )
                    }
                }
        }
    }

    fun onHabitSelected(habitId: Int) {
        _state.update { it.copy(form = HabitTaskFormHandlers.onHabitSelected(it.form, habitId)) }
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

    fun applyTemplate(template: TaskFormTemplate) {
        _state.update { it.copy(form = HabitTaskFormState.applyTemplate(it.form, template)) }
    }

    fun dismissSubmitError() {
        _state.update { it.copy(form = it.form.copy(submitError = null)) }
    }

    fun consumeCreatedTask() {
        _state.update { it.copy(createdTask = null) }
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
            habitTaskRepository.createHabitTask(request)
                .onSuccess { task ->
                    _state.update { it.copy(isSubmitting = false, createdTask = task) }
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
        else -> t.message ?: "No se pudo crear la tarea."
    }

    class Factory(
        private val habitRepository: HabitRepository,
        private val habitTaskRepository: HabitTaskRepository,
        private val preselectedHabitId: Int?,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(CreateHabitTaskViewModel::class.java))
            return CreateHabitTaskViewModel(
                habitRepository,
                habitTaskRepository,
                preselectedHabitId,
            ) as T
        }
    }
}
