package com.example.leveluplife.domain.validation

sealed class HabitTaskFieldError {
    object Required : HabitTaskFieldError()
    data class TooShort(val min: Int) : HabitTaskFieldError()
    data class TooLong(val max: Int) : HabitTaskFieldError()
    object InvalidNumber : HabitTaskFieldError()
    object CriteriaRequired : HabitTaskFieldError()
    object EvidenceRequired : HabitTaskFieldError()
}

data class HabitTaskFormInput(
    val habitId: Int?,
    val title: String,
    val description: String,
    val difficulty: String?,
    val frequency: String?,
    val periodLength: String,
    val periodUnit: String?,
    val startDate: String,
    val completionCriteria: String?,
    val repetitions: String,
    val measurementUnit: String?,
    val evidence: String?,
    val isPartialAllowed: Boolean,
)

data class HabitTaskFormErrors(
    val habitId: HabitTaskFieldError? = null,
    val title: HabitTaskFieldError? = null,
    val difficulty: HabitTaskFieldError? = null,
    val frequency: HabitTaskFieldError? = null,
    val periodLength: HabitTaskFieldError? = null,
    val startDate: HabitTaskFieldError? = null,
    val completionCriteria: HabitTaskFieldError? = null,
    val repetitions: HabitTaskFieldError? = null,
    val measurementUnit: HabitTaskFieldError? = null,
    val evidence: HabitTaskFieldError? = null,
) {
    val hasErrors: Boolean
        get() = listOfNotNull(
            habitId,
            title,
            difficulty,
            frequency,
            periodLength,
            startDate,
            completionCriteria,
            repetitions,
            measurementUnit,
            evidence,
        ).isNotEmpty()
}

object HabitTaskValidators {

    const val TITLE_MIN = 3
    const val TITLE_MAX = 100
    private val DATE_REGEX = Regex("^\\d{4}-\\d{2}-\\d{2}$")

    fun validateForm(input: HabitTaskFormInput): HabitTaskFormErrors = HabitTaskFormErrors(
        habitId = validateHabitId(input.habitId),
        title = validateTitle(input.title),
        difficulty = validateRequiredSelection(input.difficulty),
        frequency = validateRequiredSelection(input.frequency),
        periodLength = validatePeriodLength(input.periodLength),
        startDate = validateStartDate(input.startDate),
        completionCriteria = validateRequiredSelection(input.completionCriteria),
        repetitions = validateRepetitions(input),
        measurementUnit = validateMeasurementUnit(input),
        evidence = validateEvidence(input),
    )

    fun validateHabitId(habitId: Int?): HabitTaskFieldError? =
        if (habitId == null || habitId <= 0) HabitTaskFieldError.Required else null

    fun validateTitle(title: String): HabitTaskFieldError? {
        val trimmed = title.trim()
        return when {
            trimmed.isEmpty() -> HabitTaskFieldError.Required
            trimmed.length < TITLE_MIN -> HabitTaskFieldError.TooShort(TITLE_MIN)
            trimmed.length > TITLE_MAX -> HabitTaskFieldError.TooLong(TITLE_MAX)
            else -> null
        }
    }

    fun validateRequiredSelection(value: String?): HabitTaskFieldError? =
        if (value.isNullOrBlank()) HabitTaskFieldError.Required else null

    fun validatePeriodLength(value: String): HabitTaskFieldError? {
        if (value.isBlank()) return null
        val parsed = value.toIntOrNull() ?: return HabitTaskFieldError.InvalidNumber
        return if (parsed < 1) HabitTaskFieldError.InvalidNumber else null
    }

    fun validateStartDate(value: String): HabitTaskFieldError? {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> HabitTaskFieldError.Required
            !DATE_REGEX.matches(trimmed) -> HabitTaskFieldError.InvalidNumber
            else -> null
        }
    }

    fun validateRepetitions(input: HabitTaskFormInput): HabitTaskFieldError? {
        if (input.completionCriteria != "REPETITIONS") return null
        if (input.repetitions.isBlank()) return HabitTaskFieldError.CriteriaRequired
        val parsed = input.repetitions.toIntOrNull() ?: return HabitTaskFieldError.InvalidNumber
        return if (parsed < 1) HabitTaskFieldError.InvalidNumber else null
    }

    fun validateMeasurementUnit(input: HabitTaskFormInput): HabitTaskFieldError? {
        if (input.completionCriteria != "REPETITIONS") return null
        return if (input.measurementUnit.isNullOrBlank()) HabitTaskFieldError.CriteriaRequired else null
    }

    fun validateEvidence(input: HabitTaskFormInput): HabitTaskFieldError? {
        if (input.completionCriteria != "EVIDENCE") return null
        return if (input.evidence.isNullOrBlank()) HabitTaskFieldError.EvidenceRequired else null
    }
}
