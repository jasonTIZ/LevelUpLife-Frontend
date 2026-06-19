package com.example.leveluplife.ui.createtask

import com.example.leveluplife.data.network.dto.CreateHabitTaskRequest
import com.example.leveluplife.data.network.dto.CreateRepetitionCriteriaRequest
import com.example.leveluplife.data.network.dto.CreateTimerCriteriaRequest
import com.example.leveluplife.data.network.dto.HabitDto
import com.example.leveluplife.data.network.dto.HabitTaskDto
import com.example.leveluplife.domain.validation.HabitTaskFormErrors
import com.example.leveluplife.domain.validation.HabitTaskFormInput
import com.example.leveluplife.domain.validation.HabitTaskValidators
import com.example.leveluplife.domain.validation.TaskInputSanitizer

data class HabitTaskFormState(
    val habits: List<HabitDto> = emptyList(),
    val selectedHabitId: Int? = null,
    val title: String = "",
    val description: String = "",
    val difficulty: String? = "MEDIUM",
    val frequency: String? = "WEEKLY",
    val periodLength: String = "1",
    val periodUnit: String? = "WEEKS",
    val startDate: String = "",
    val completionCriteria: String? = "REPETITIONS",
    val repetitions: String = "3",
    val measurementUnit: String? = "SERIES",
    val evidence: String? = null,
    val isPartialAllowed: Boolean = true,
    val timerSecondsDefined: String = "",
    val timerSecondsLong: String = "",
    val timerPauseAllowed: Boolean = false,
    val selectedTemplateId: String? = null,
    val fieldErrors: HabitTaskFormErrors = HabitTaskFormErrors(),
    val showValidationErrors: Boolean = false,
    val submitError: String? = null,
) {
    val selectedHabit: HabitDto?
        get() = habits.firstOrNull { it.id == selectedHabitId }

    fun sanitizedForValidation(): HabitTaskFormState = copy(
        title = title.trim(),
        description = description.trim(),
        periodLength = periodLength.trim(),
        startDate = startDate.trim(),
        repetitions = repetitions.trim(),
        timerSecondsDefined = timerSecondsDefined.trim(),
        timerSecondsLong = timerSecondsLong.trim(),
    )

    fun toFormInput(): HabitTaskFormInput = HabitTaskFormInput(
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
        timerSecondsDefined = timerSecondsDefined,
        timerSecondsLong = timerSecondsLong,
    )

    fun toRequest(): CreateHabitTaskRequest? {
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

        val timerCriteria = when (criteria) {
            "TIMER" -> {
                val seconds = timerSecondsDefined.toIntOrNull() ?: return null
                CreateTimerCriteriaRequest(
                    numSecondsDefined = seconds,
                    numSecondsLong = timerSecondsLong.toIntOrNull(),
                    typePauseIsAllowed = timerPauseAllowed,
                    statusTimerCriteriaIsActive = true,
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
            timerCriteria = timerCriteria,
            isActive = true,
        )
    }

    companion object {
        fun fromTask(task: HabitTaskDto, habits: List<HabitDto>): HabitTaskFormState = HabitTaskFormState(
            habits = habits,
            selectedHabitId = task.habitId,
            title = task.title,
            description = task.description.orEmpty(),
            difficulty = task.difficulty,
            frequency = task.frequency,
            periodLength = task.periodLength.toString(),
            periodUnit = task.periodUnit,
            startDate = task.startDate,
            completionCriteria = task.completionCriteria,
            repetitions = task.repetitionCriteria?.repetitions?.toString().orEmpty(),
            measurementUnit = task.repetitionCriteria?.measurementUnit,
            evidence = task.evidence,
            isPartialAllowed = task.repetitionCriteria?.isPartialAllowed ?: false,
            timerSecondsDefined = task.timerCriteria?.numSecondsDefined?.toString().orEmpty(),
            timerSecondsLong = task.timerCriteria?.numSecondsLong?.toString().orEmpty(),
            timerPauseAllowed = task.timerCriteria?.typePauseIsAllowed ?: false,
        )

        fun applyTemplate(current: HabitTaskFormState, template: TaskFormTemplate): HabitTaskFormState =
            current.copy(
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

object HabitTaskFormHandlers {
    fun onTitleChange(state: HabitTaskFormState, value: String): HabitTaskFormState =
        state.copy(
            title = TaskInputSanitizer.trimToMax(value, HabitTaskValidators.TITLE_MAX),
            fieldErrors = state.fieldErrors.copy(title = null),
            submitError = null,
        )

    fun onDescriptionChange(state: HabitTaskFormState, value: String): HabitTaskFormState =
        state.copy(
            description = TaskInputSanitizer.trimToMax(value, HabitTaskValidators.DESCRIPTION_MAX),
            submitError = null,
        )

    fun onDifficultyChange(state: HabitTaskFormState, value: String): HabitTaskFormState =
        state.copy(
            difficulty = value,
            fieldErrors = state.fieldErrors.copy(difficulty = null),
            submitError = null,
        )

    fun onFrequencyChange(state: HabitTaskFormState, value: String): HabitTaskFormState =
        state.copy(
            frequency = value,
            fieldErrors = state.fieldErrors.copy(frequency = null),
            submitError = null,
        )

    fun onPeriodLengthChange(state: HabitTaskFormState, value: String): HabitTaskFormState =
        state.copy(
            periodLength = TaskInputSanitizer.digitsOnly(value),
            fieldErrors = state.fieldErrors.copy(periodLength = null),
            submitError = null,
        )

    fun onPeriodUnitChange(state: HabitTaskFormState, value: String): HabitTaskFormState =
        state.copy(
            periodUnit = value,
            fieldErrors = state.fieldErrors.copy(periodUnit = null),
            submitError = null,
        )

    fun onStartDateChange(state: HabitTaskFormState, value: String): HabitTaskFormState =
        state.copy(
            startDate = TaskInputSanitizer.isoDateInput(value),
            fieldErrors = state.fieldErrors.copy(startDate = null),
            submitError = null,
        )

    fun onCompletionCriteriaChange(state: HabitTaskFormState, value: String): HabitTaskFormState =
        state.copy(
            completionCriteria = value,
            repetitions = if (value == "REPETITIONS" && state.repetitions.isBlank()) "3" else state.repetitions,
            measurementUnit = if (value == "REPETITIONS") state.measurementUnit ?: "SERIES" else null,
            evidence = if (value == "EVIDENCE") state.evidence ?: "PHOTO" else null,
            timerSecondsDefined = if (value == "TIMER" && state.timerSecondsDefined.isBlank()) {
                "60"
            } else {
                state.timerSecondsDefined
            },
            fieldErrors = state.fieldErrors.copy(
                completionCriteria = null,
                repetitions = null,
                measurementUnit = null,
                evidence = null,
                timerSeconds = null,
                timerLong = null,
            ),
            submitError = null,
        )

    fun onTimerSecondsDefinedChange(state: HabitTaskFormState, value: String): HabitTaskFormState =
        state.copy(
            timerSecondsDefined = TaskInputSanitizer.digitsOnly(value, maxLength = 5),
            fieldErrors = state.fieldErrors.copy(timerSeconds = null, timerLong = null),
            submitError = null,
        )

    fun onTimerSecondsLongChange(state: HabitTaskFormState, value: String): HabitTaskFormState =
        state.copy(
            timerSecondsLong = TaskInputSanitizer.digitsOnly(value, maxLength = 5),
            fieldErrors = state.fieldErrors.copy(timerLong = null),
            submitError = null,
        )

    fun onTimerPauseAllowedChange(state: HabitTaskFormState, value: Boolean): HabitTaskFormState =
        state.copy(
            timerPauseAllowed = value,
            submitError = null,
        )

    fun onRepetitionsChange(state: HabitTaskFormState, value: String): HabitTaskFormState =
        state.copy(
            repetitions = TaskInputSanitizer.digitsOnly(value, maxLength = 5),
            fieldErrors = state.fieldErrors.copy(repetitions = null),
            submitError = null,
        )

    fun onMeasurementUnitChange(state: HabitTaskFormState, value: String): HabitTaskFormState =
        state.copy(
            measurementUnit = value,
            fieldErrors = state.fieldErrors.copy(measurementUnit = null),
            submitError = null,
        )

    fun onEvidenceChange(state: HabitTaskFormState, value: String): HabitTaskFormState =
        state.copy(
            evidence = value,
            fieldErrors = state.fieldErrors.copy(evidence = null),
            submitError = null,
        )

    fun onHabitSelected(state: HabitTaskFormState, habitId: Int): HabitTaskFormState =
        state.copy(
            selectedHabitId = habitId,
            fieldErrors = state.fieldErrors.copy(habitId = null),
            showValidationErrors = false,
            submitError = null,
        )
}
