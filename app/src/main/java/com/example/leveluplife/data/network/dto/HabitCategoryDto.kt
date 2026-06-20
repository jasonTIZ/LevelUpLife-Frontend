package com.example.leveluplife.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class HabitCategoryDto(
    val id: Int,
    val name: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val habitsCount: Int = 0,
    val isActive: Boolean = false,
)

@Serializable
data class HabitCategoriesPagedData(
    val items: List<HabitCategoryDto> = emptyList(),
    val totalRecords: Int = 0,
    val totalPages: Int = 0,
    val currentPage: Int = 1,
    val pageSize: Int = 10,
)

@Serializable
data class HabitCategoriesPageResponse(
    val success: Boolean = false,
    val data: HabitCategoriesPagedData? = null,
    val pagination: PaginationDto? = null,
    val message: String? = null,
) {
    val categories: List<HabitCategoryDto>
        get() = data?.items ?: emptyList()

    val resolvedPagination: PaginationDto?
        get() = pagination ?: data?.let {
            PaginationDto(
                currentPage = it.currentPage,
                pageSize = it.pageSize,
                totalPages = it.totalPages,
                totalRecords = it.totalRecords,
            )
        }
}
