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
