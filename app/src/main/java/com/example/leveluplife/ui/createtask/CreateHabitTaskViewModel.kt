package com.example.leveluplife.ui.createtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.habits.HabitTaskRepository
import com.example.leveluplife.data.habits.HabitTaskValidationFailure
import com.example.leveluplife.data.network.dto.CreateHabitTaskRequest
import com.example.leveluplife.data.network.dto.CreateRepetitionCriteriaRequest
import com.example.leveluplife.domain.validation.HabitTaskFormErrors
import com.example.leveluplife.domain.validation.HabitTaskFormInput
import com.example.leveluplife.domain.validation.HabitTaskValidators
import com.example.leveluplife.domain.validation.TaskInputSanitizer
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
            selectedHabitId = preselectedHabitId?.takeIf { it > 0 },
            startDate = LocalDate.now().toString(),
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
                        val selected = current.selectedHabitId
                            ?: habits.firstOrNull()?.id
                        current.copy(
                            isLoadingHabits = false,
                            habits = habits,
                            selectedHabitId = selected,
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
        _state.update {
            it.copy(
                selectedHabitId = habitId,
                fieldErrors = it.fieldErrors.copy(habitId = null),
                showValidationErrors = false,
                submitError = null,
            )
        }
    }

    fun onTitleChange(value: String) {
        _state.update {
            it.copy(
                title = TaskInputSanitizer.trimToMax(value, HabitTaskValidators.TITLE_MAX),
                fieldErrors = it.fieldErrors.copy(title = null),
                submitError = null,
            )
        }
    }

    fun onDescriptionChange(value: String) {
        _state.update {
            it.copy(
                description = TaskInputSanitizer.trimToMax(value, 500),
                submitError = null,
            )
        }
    }

    fun onDifficultyChange(value: String) {
        _state.update {
            it.copy(
                difficulty = value,
                fieldErrors = it.fieldErrors.copy(difficulty = null),
                submitError = null,
            )
        }
    }

    fun onFrequencyChange(value: String) {
        _state.update {
            it.copy(
                frequency = value,
                fieldErrors = it.fieldErrors.copy(frequency = null),
                submitError = null,
            )
        }
    }

    fun onPeriodLengthChange(value: String) {
        _state.update {
            it.copy(
                periodLength = TaskInputSanitizer.digitsOnly(value),
                fieldErrors = it.fieldErrors.copy(periodLength = null),
                submitError = null,
            )
        }
    }

    fun onPeriodUnitChange(value: String) {
        _state.update {
            it.copy(
                periodUnit = value,
                fieldErrors = it.fieldErrors.copy(periodUnit = null),
                submitError = null,
            )
        }
    }

    fun onStartDateChange(value: String) {
        _state.update {
            it.copy(
                startDate = TaskInputSanitizer.isoDateInput(value),
                fieldErrors = it.fieldErrors.copy(startDate = null),
                submitError = null,
            )
        }
    }

    fun onCompletionCriteriaChange(value: String) {
        _state.update {
            it.copy(
                completionCriteria = value,
                repetitions = if (value == "REPETITIONS" && it.repetitions.isBlank()) "3" else it.repetitions,
                measurementUnit = if (value == "REPETITIONS") it.measurementUnit ?: "SERIES" else null,
                evidence = if (value == "EVIDENCE") it.evidence ?: "PHOTO" else null,
                fieldErrors = it.fieldErrors.copy(
                    completionCriteria = null,
                    repetitions = null,
                    measurementUnit = null,
                    evidence = null,
                ),
                submitError = null,
            )
        }
    }

    fun onRepetitionsChange(value: String) {
        _state.update {
            it.copy(
                repetitions = TaskInputSanitizer.digitsOnly(value, maxLength = 5),
                fieldErrors = it.fieldErrors.copy(repetitions = null),
                submitError = null,
            )
        }
    }

    fun onMeasurementUnitChange(value: String) {
        _state.update {
            it.copy(
                measurementUnit = value,
                fieldErrors = it.fieldErrors.copy(measurementUnit = null),
                submitError = null,
            )
        }
    }

    fun onEvidenceChange(value: String) {
        _state.update {
            it.copy(
                evidence = value,
                fieldErrors = it.fieldErrors.copy(evidence = null),
                submitError = null,
            )
        }
    }

    fun onPartialAllowedChange(value: Boolean) {
        _state.update { it.copy(isPartialAllowed = value) }
    }

    fun applyTemplate(template: TaskFormTemplate) {
        _state.update {
            it.copy(
                title = template.title,
                description = template.description,
                difficulty = template.difficulty,
                frequency = template.frequency,
                periodLength = template.periodLength,
                periodUnit = template.periodUnit,
                completionCriteria = template.completionCriteria,
                repetitions = template.repetitions,
                measurementUnit = template.measurementUnit,
                evidence = template.evidence,
                isPartialAllowed = template.isPartialAllowed,
                selectedTemplateId = template.id,
                fieldErrors = HabitTaskFormErrors(),
                showValidationErrors = false,
                submitError = null,
            )
        }
    }

    fun dismissSubmitError() {
        _state.update { it.copy(submitError = null) }
    }

    fun consumeCreatedTask() {
        _state.update { it.copy(createdTask = null) }
    }

    fun submit() {
        val sanitized = _state.value.sanitizedForValidation()
        val errors = HabitTaskValidators.validateForm(sanitized.toFormInput())
        if (errors.hasErrors) {
            _state.update {
                sanitized.copy(
                    fieldErrors = errors,
                    showValidationErrors = true,
                    submitError = null,
                )
            }
            return
        }

        _state.update { sanitized }

        val request = _state.value.toRequest()
        if (request == null) {
            _state.update {
                it.copy(
                    showValidationErrors = true,
                    submitError = "No se pudo enviar la tarea. Revisá que todos los campos estén completos.",
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(isSubmitting = true, submitError = null)
            }
            habitTaskRepository.createHabitTask(request)
                .onSuccess { task ->
                    _state.update { it.copy(isSubmitting = false, createdTask = task) }
                }
                .onFailure { t ->
                    when (t) {
                        is HabitTaskValidationFailure -> _state.update {
                            it.copy(
                                isSubmitting = false,
                                fieldErrors = t.fieldErrors,
                                showValidationErrors = true,
                                submitError = null,
                            )
                        }
                        else -> _state.update {
                            it.copy(
                                isSubmitting = false,
                                submitError = mapSubmitError(t),
                            )
                        }
                    }
                }
        }
    }

    private fun CreateHabitTaskUiState.sanitizedForValidation(): CreateHabitTaskUiState = copy(
        title = title.trim(),
        description = description.trim(),
        periodLength = periodLength.trim(),
        startDate = startDate.trim(),
        repetitions = repetitions.trim(),
    )

    private fun CreateHabitTaskUiState.toFormInput(): HabitTaskFormInput = HabitTaskFormInput(
        habitId = selectedHabitId,
        title = title,
        description = description,
        difficulty = difficulty,
        frequency = frequency,
        periodLength = periodLength,
        periodUnit = periodUnit,
        startDate = startDate,
        completionCriteria = completionCriteria,
        repetitions = repetitions,
        measurementUnit = measurementUnit,
        evidence = evidence,
        isPartialAllowed = isPartialAllowed,
    )

    private fun CreateHabitTaskUiState.toRequest(): CreateHabitTaskRequest? {
        val habitId = selectedHabitId ?: return null
        val criteria = completionCriteria ?: return null
        val period = periodLength.toIntOrNull() ?: return null
        val periodUnitValue = periodUnit ?: return null

        val repetitionCriteria = when (criteria) {
            "REPETITIONS" -> {
                val reps = repetitions.toIntOrNull() ?: return null
                val unit = measurementUnit ?: return null
                CreateRepetitionCriteriaRequest(
                    repetitions = reps,
                    measurementUnit = unit,
                    isPartialAllowed = isPartialAllowed,
                    isActive = true,
                )
            }
            else -> null
        }

        if (criteria == "EVIDENCE" && evidence.isNullOrBlank()) return null

        return CreateHabitTaskRequest(
            habitId = habitId,
            title = title.trim(),
            description = description.trim().ifBlank { null },
            difficulty = difficulty!!,
            frequency = frequency!!,
            periodLength = period,
            periodUnit = periodUnitValue,
            startDate = startDate.trim(),
            completionCriteria = criteria,
            evidence = if (criteria == "EVIDENCE") evidence else null,
            repetitionCriteria = repetitionCriteria,
            isActive = true,
        )
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
