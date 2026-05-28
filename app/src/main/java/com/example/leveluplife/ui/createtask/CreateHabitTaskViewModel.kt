package com.example.leveluplife.ui.createtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.habits.HabitTaskRepository
import com.example.leveluplife.data.network.dto.CreateHabitTaskRequest
import com.example.leveluplife.data.network.dto.CreateRepetitionCriteriaRequest
import com.example.leveluplife.domain.validation.HabitTaskFormInput
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
        _state.update { it.copy(selectedHabitId = habitId, showValidationErrors = false) }
    }

    fun onTitleChange(value: String) {
        _state.update { it.copy(title = value) }
    }

    fun onDescriptionChange(value: String) {
        _state.update { it.copy(description = value) }
    }

    fun onDifficultyChange(value: String) {
        _state.update { it.copy(difficulty = value) }
    }

    fun onFrequencyChange(value: String) {
        _state.update { it.copy(frequency = value) }
    }

    fun onPeriodLengthChange(value: String) {
        _state.update { it.copy(periodLength = value) }
    }

    fun onPeriodUnitChange(value: String) {
        _state.update { it.copy(periodUnit = value) }
    }

    fun onStartDateChange(value: String) {
        _state.update { it.copy(startDate = value) }
    }

    fun onCompletionCriteriaChange(value: String) {
        _state.update {
            it.copy(
                completionCriteria = value,
                repetitions = if (value == "REPETITIONS" && it.repetitions.isBlank()) "3" else it.repetitions,
                measurementUnit = if (value == "REPETITIONS") it.measurementUnit ?: "SERIES" else null,
                evidence = if (value == "EVIDENCE") it.evidence ?: "PHOTO" else null,
            )
        }
    }

    fun onRepetitionsChange(value: String) {
        _state.update { it.copy(repetitions = value) }
    }

    fun onMeasurementUnitChange(value: String) {
        _state.update { it.copy(measurementUnit = value) }
    }

    fun onEvidenceChange(value: String) {
        _state.update { it.copy(evidence = value) }
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
        val current = _state.value
        val errors = HabitTaskValidators.validateForm(current.toFormInput())
        if (errors.hasErrors) {
            _state.update { it.copy(fieldErrors = errors, showValidationErrors = true) }
            return
        }

        val request = current.toRequest() ?: return

        viewModelScope.launch {
            _state.update {
                it.copy(isSubmitting = true, submitError = null, showValidationErrors = false)
            }
            habitTaskRepository.createHabitTask(request)
                .onSuccess { task ->
                    _state.update { it.copy(isSubmitting = false, createdTask = task) }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(isSubmitting = false, submitError = mapSubmitError(t))
                    }
                }
        }
    }

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
        return CreateHabitTaskRequest(
            habitId = habitId,
            title = title.trim(),
            description = description.trim().ifBlank { null },
            difficulty = difficulty!!,
            frequency = frequency!!,
            periodLength = periodLength.toIntOrNull() ?: 1,
            periodUnit = periodUnit,
            startDate = startDate.trim(),
            completionCriteria = criteria,
            evidence = if (criteria == "EVIDENCE") evidence else null,
            repetitionCriteria = if (criteria == "REPETITIONS") {
                CreateRepetitionCriteriaRequest(
                    repetitions = repetitions.toIntOrNull() ?: 1,
                    measurementUnit = measurementUnit!!,
                    isPartialAllowed = isPartialAllowed,
                    isActive = true,
                )
            } else {
                null
            },
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
