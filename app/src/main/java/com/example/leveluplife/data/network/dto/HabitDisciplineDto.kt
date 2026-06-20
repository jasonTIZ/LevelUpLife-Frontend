package com.example.leveluplife.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HabitDisciplineDto(
    @SerialName("idHabitDiscipline") val id: Int,
    @SerialName("idHabitCategory") val categoryId: Int,
    @SerialName("dscHabitDisciplineName") val name: String,
    @SerialName("dscHabitDisciplineDescription") val description: String = "",
    @SerialName("statusHabitDisciplineIsActive") val isActive: Boolean = true,
)
