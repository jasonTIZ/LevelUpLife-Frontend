package com.example.leveluplife.domain.validation

import java.time.LocalDate

sealed class HabitTaskFieldError {
    object Required : HabitTaskFieldError()
    data class TooShort(val min: Int) : HabitTaskFieldError()
    data class TooLong(val max: Int) : HabitTaskFieldError()
    object InvalidNumber : HabitTaskFieldError()
    object InvalidDate : HabitTaskFieldError()
    object PastDate : HabitTaskFieldError()
    object InvalidOption : HabitTaskFieldError()
    object CriteriaRequired : HabitTaskFieldError()
    object EvidenceRequired : HabitTaskFieldError()
    object TimerThresholdTooSmall : HabitTaskFieldError()
}

data class HabitTaskValidationOptions(
    val today: LocalDate = LocalDate.now(),
    val preservedStartDate: String? = null,
)

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
    val timerSecondsDefined: String,
    val timerSecondsLong: String = "",
)

data class HabitTaskFormErrors(
    val habitId: HabitTaskFieldError? = null,
    val title: HabitTaskFieldError? = null,
    val difficulty: HabitTaskFieldError? = null,
    val frequency: HabitTaskFieldError? = null,
    val periodLength: HabitTaskFieldError? = null,
    val periodUnit: HabitTaskFieldError? = null,
    val startDate: HabitTaskFieldError? = null,
    val completionCriteria: HabitTaskFieldError? = null,
    val repetitions: HabitTaskFieldError? = null,
    val measurementUnit: HabitTaskFieldError? = null,
    val evidence: HabitTaskFieldError? = null,
    val timerSeconds: HabitTaskFieldError? = null,
    val timerLong: HabitTaskFieldError? = null,
) {
    val hasErrors: Boolean
        get() = listOfNotNull(
            habitId,
            title,
            difficulty,
            frequency,
            periodLength,
            periodUnit,
            startDate,
            completionCriteria,
            repetitions,
            measurementUnit,
            evidence,
            timerSeconds,
            timerLong,
        ).isNotEmpty()
}

object HabitTaskValidators {

    const val TITLE_MIN = 3
    const val TITLE_MAX = 100
    const val DESCRIPTION_MAX = 500
    const val PERIOD_LENGTH_MAX = 9999
    const val REPETITIONS_MAX = 99_999
    const val TIMER_SECONDS_MAX = 86_400

    val DIFFICULTIES = setOf("EASY", "MEDIUM", "HARD", "EPIC")
    val FREQUENCIES = setOf("DAILY", "WEEKLY", "MONTHLY")
    val PERIOD_UNITS = setOf("DAYS", "WEEKS", "MONTHS")
    val COMPLETION_CRITERIA = setOf("REPETITIONS", "EVIDENCE", "TIMER")
    val MEASUREMENT_UNITS = setOf("REPS", "SERIES", "KMS", "CALS")
    val EVIDENCE_TYPES = setOf("PHOTO", "VIDEO", "HEALTH_CONNECT")

    private val DATE_REGEX = Regex("^\\d{4}-\\d{2}-\\d{2}$")

    fun validateForm(
        input: HabitTaskFormInput,
        options: HabitTaskValidationOptions = HabitTaskValidationOptions(),
    ): HabitTaskFormErrors = HabitTaskFormErrors(
        habitId = validateHabitId(input.habitId),
        title = validateTitle(input.title),
        difficulty = validateOption(input.difficulty, DIFFICULTIES),
        frequency = validateOption(input.frequency, FREQUENCIES),
        periodLength = validatePeriodLength(input.periodLength),
        periodUnit = validateOption(input.periodUnit, PERIOD_UNITS),
        startDate = validateStartDate(input.startDate, options),
        completionCriteria = validateOption(input.completionCriteria, COMPLETION_CRITERIA),
        repetitions = validateRepetitions(input),
        measurementUnit = validateMeasurementUnit(input),
        evidence = validateEvidence(input),
        timerSeconds = validateTimerSeconds(input),
        timerLong = validateTimerLong(input),
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

    fun validateOption(value: String?, allowed: Set<String>): HabitTaskFieldError? {
        if (value.isNullOrBlank()) return HabitTaskFieldError.Required
        return if (value in allowed) null else HabitTaskFieldError.InvalidOption
    }

    fun validatePeriodLength(value: String): HabitTaskFieldError? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return HabitTaskFieldError.Required
        val parsed = trimmed.toIntOrNull() ?: return HabitTaskFieldError.InvalidNumber
        return when {
            parsed < 1 -> HabitTaskFieldError.InvalidNumber
            parsed > PERIOD_LENGTH_MAX -> HabitTaskFieldError.InvalidNumber
            else -> null
        }
    }

    fun validateStartDate(
        value: String,
        options: HabitTaskValidationOptions = HabitTaskValidationOptions(),
    ): HabitTaskFieldError? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return HabitTaskFieldError.Required
        if (!DATE_REGEX.matches(trimmed)) return HabitTaskFieldError.InvalidDate

        val parsed = runCatching { LocalDate.parse(trimmed) }
            .getOrElse { return HabitTaskFieldError.InvalidDate }

        val preservedDate = options.preservedStartDate
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { preserved -> runCatching { LocalDate.parse(preserved) }.getOrNull() }
        if (preservedDate != null && parsed == preservedDate) return null
        if (parsed.isBefore(options.today)) return HabitTaskFieldError.PastDate
        return null
    }

    fun validateRepetitions(input: HabitTaskFormInput): HabitTaskFieldError? {
        if (input.completionCriteria != "REPETITIONS") return null
        if (input.repetitions.isBlank()) return HabitTaskFieldError.CriteriaRequired
        val parsed = input.repetitions.toIntOrNull() ?: return HabitTaskFieldError.InvalidNumber
        return when {
            parsed < 1 -> HabitTaskFieldError.InvalidNumber
            parsed > REPETITIONS_MAX -> HabitTaskFieldError.InvalidNumber
            else -> null
        }
    }

    fun validateMeasurementUnit(input: HabitTaskFormInput): HabitTaskFieldError? {
        if (input.completionCriteria != "REPETITIONS") return null
        if (input.measurementUnit.isNullOrBlank()) return HabitTaskFieldError.CriteriaRequired
        return validateOption(input.measurementUnit, MEASUREMENT_UNITS)
    }

    fun validateEvidence(input: HabitTaskFormInput): HabitTaskFieldError? {
        if (input.completionCriteria != "EVIDENCE") return null
        if (input.evidence.isNullOrBlank()) return HabitTaskFieldError.EvidenceRequired
        return validateOption(input.evidence, EVIDENCE_TYPES)
    }

    fun validateTimerSeconds(input: HabitTaskFormInput): HabitTaskFieldError? {
        if (input.completionCriteria != "TIMER") return null
        if (input.timerSecondsDefined.isBlank()) return HabitTaskFieldError.CriteriaRequired
        val parsed = input.timerSecondsDefined.toIntOrNull() ?: return HabitTaskFieldError.InvalidNumber
        return when {
            parsed < 1 -> HabitTaskFieldError.InvalidNumber
            parsed > TIMER_SECONDS_MAX -> HabitTaskFieldError.InvalidNumber
            else -> null
        }
    }

    /** Optional long threshold: when present it must be valid and exceed the base duration. */
    fun validateTimerLong(input: HabitTaskFormInput): HabitTaskFieldError? {
        if (input.completionCriteria != "TIMER") return null
        if (input.timerSecondsLong.isBlank()) return null
        val parsed = input.timerSecondsLong.toIntOrNull() ?: return HabitTaskFieldError.InvalidNumber
        if (parsed < 1 || parsed > TIMER_SECONDS_MAX) return HabitTaskFieldError.InvalidNumber
        val base = input.timerSecondsDefined.toIntOrNull()
        if (base != null && parsed <= base) return HabitTaskFieldError.TimerThresholdTooSmall
        return null
    }
}
