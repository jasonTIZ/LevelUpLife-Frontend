package com.example.leveluplife.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateHabitRequestDto(
    val id: Int,
    val title: String,
    val description: String = "",
    @SerialName("userId") val userId: Int,
    @SerialName("newTasks") val newTasks: List<CreateHabitTaskRequestDto> = emptyList(),
)
