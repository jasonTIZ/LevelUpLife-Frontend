package com.example.leveluplife.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HabitDto(
    val id: Int,
    val title: String = "",
    val description: String = "",
    val disciplineName: String = "",
    val userName: String = "",
    val categoryName: String = "",
    val isActive: Boolean = false,
    val tasks: List<HabitTaskDto> = emptyList(),
)

@Serializable
data class RepetitionCriteriaDto(
    val id: Int,
    val habitTaskId: Int,
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
data class HabitTaskDto(
    val id: Int,
    val repetitionCriteria: RepetitionCriteriaDto? = null,
)

@Serializable
data class PaginationDto(
    val currentPage: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalRecords: Int,
)

@Serializable
data class HabitsPageResponse(
    val success: Boolean = false,
    @SerialName("data")
    val habits: List<HabitDto>? = null,
    val pagination: PaginationDto? = null,
    val message: String? = null,
)
