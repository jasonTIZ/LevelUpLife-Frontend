package com.example.leveluplife.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateHabitTaskRequestDto(
    val title: String,
    val description: String? =null,
    @SerialName("habitDisciplineId") val habitDisciplineId: Int? = null,
    @SerialName("weekDays") val weekDays: String? = null,
    val difficulty: TaskDifficulty,
    val frequency: TaskFrequency,
    @SerialName("periodLength") val periodLength: Int,
    @SerialName("periodUnit") val periodUnit: TaskPeriodUnit,
    @SerialName("startDate") val startDate: String,
    @SerialName("completionCriteria") val completionCriteria: TaskCompletionCriteria,
    val evidence: TaskEvidence? = null,
    @SerialName("repetitionCriteria") val repetitionCriteria: RepetitionCriteriaRequestDto? = null,
    @SerialName("timerCriteria") val timerCriteria: TimerCriteriaRequestDto? = null,
    @SerialName("xpValue") val xpValue: Int? = null,
    @SerialName("isActive") val isActive: Boolean? = null
)
