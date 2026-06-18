package com.example.leveluplife.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateRepetitionCriteriaRequest(
    val repetitions: Int,
    val measurementUnit: String,
    val isPartialAllowed: Boolean = false,
    val isActive: Boolean = true,
)

/** Names aligned with the backend CreateTimerCriteriaRequestDto. */
@Serializable
data class CreateTimerCriteriaRequest(
    @SerialName("NUM_SECONDS_DEFINED")
    val numSecondsDefined: Int,
    @SerialName("NUM_SECONDS_LONG")
    val numSecondsLong: Int? = null,
    @SerialName("TYPE_PAUSE_IS_ALLOWED")
    val typePauseIsAllowed: Boolean,
    @SerialName("STATUS_TIMER_CRITERIA_IS_ACTIVE")
    val statusTimerCriteriaIsActive: Boolean = true,
)

@Serializable
data class CreateHabitTaskRequest(
    val habitId: Int,
    val habitDisciplineId: Int? = null,
    val title: String,
    val description: String? = null,
    val xpValue: Int? = null,
    val periodLength: Int? = null,
    val startDate: String? = null,
    val isActive: Boolean? = true,
    val weekDays: String? = null,
    val difficulty: String,
    val frequency: String,
    val periodUnit: String? = null,
    val completionCriteria: String,
    val evidence: String? = null,
    val repetitionCriteria: CreateRepetitionCriteriaRequest? = null,
    val timerCriteria: CreateTimerCriteriaRequest? = null,
)

@Serializable
data class RepetitionCriteriaDto(
    val id: Int = 0,
    val habitTaskId: Int = 0,
    val repetitions: Int = 0,
    val measurementUnit: String = "",
    val isPartialAllowed: Boolean = false,
    val isActive: Boolean = false,
) {
    fun toReadableSummary(): String {
        val unit = when (measurementUnit.uppercase()) {
            "REPS" -> if (repetitions == 1) "repetición" else "repeticiones"
            "SERIES" -> if (repetitions == 1) "serie" else "series"
            "KMS" -> "km"
            "CALS" -> "cal"
            else -> measurementUnit.lowercase()
        }
        return "$repetitions $unit"
    }
}

@Serializable
data class TimerCriteriaDto(
    val id: Int = 0,
    val habitTaskId: Int = 0,
    val numSecondsDefined: Int = 0,
    val numSecondsLong: Int? = null,
    val typePauseIsAllowed: Boolean = false,
    val statusTimerCriteriaIsActive: Boolean = true,
)

@Serializable
data class HabitTaskDto(
    val id: Int,
    val habitId: Int = 0,
    val habitDisciplineId: Int? = null,
    val title: String = "",
    val description: String? = null,
    val xpValue: Int = 0,
    val periodLength: Int = 1,
    val periodUnit: String = "DAYS",
    val startDate: String = "",
    val isCompleted: Boolean = false,
    val isActive: Boolean = true,
    val weekDays: String? = null,
    val difficulty: String = "EASY",
    val frequency: String = "DAILY",
    val completionCriteria: String = "REPETITIONS",
    val evidence: String? = null,
    val repetitionCriteria: RepetitionCriteriaDto? = null,
    val timerCriteria: TimerCriteriaDto? = null,
)
