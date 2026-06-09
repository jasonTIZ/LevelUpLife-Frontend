package com.example.leveluplife.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateHabitRequest(
    val title: String,
    val description: String? = null,
    @SerialName("disciplineId") val disciplineId: Int,
    @SerialName("userId") val userId: Int,
    val tasks: List<CreateHabitTaskRequestDto>,
)