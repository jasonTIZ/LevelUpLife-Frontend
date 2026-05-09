package com.example.leveluplife.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class DisciplineResponse(
    val success: Boolean = false,
    val data: DisciplineData? = null,
    val message: String? = null,
)

@Serializable
data class DisciplineData(
    val id: String,
    val name: String,
    val description: String? = null,
    val iconUrl: String? = null,
    val category: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class HabitsApiResponse(
    val success: Boolean = false,
    val data: List<HabitData>? = null,
    val message: String? = null,
    val totalItems: Int? = null,
    val currentPage: Int? = null,
    val totalPages: Int? = null,
    val total: Int? = null,
    val page: Int? = null,
)

@Serializable
data class HabitData(
    val id: String,
    val name: String,
    val description: String? = null,
    val disciplineId: String = "",
    val frequency: String? = null,
    val difficulty: String? = null,
    val xpReward: Int = 0,
)
