package com.example.leveluplife.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class HabitCategoryDto(
    val id: Int,
    val name: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val habitsCount: Int = 0,
    val isActive: Boolean = true,
)

@Serializable
data class HabitCategoriesPageResponse(
    val success: Boolean = false,
    val data: List<HabitCategoryDto>? = null,
    val pagination: PaginationDto? = null,
    val message: String? = null,
) {
    val categories: List<HabitCategoryDto>
        get() = data.orEmpty()

    val resolvedPagination: PaginationDto?
        get() = pagination
}
