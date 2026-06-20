package com.example.leveluplife.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateHabitResponseDto(
    val success: Boolean,
    val message: String,
    val aiDifficultyFailed: Boolean = false,
)
