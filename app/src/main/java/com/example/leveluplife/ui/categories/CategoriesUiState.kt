package com.example.leveluplife.ui.categories

import com.example.leveluplife.data.network.dto.HabitCategoryDto

data class CategoriesUiState(
    val categories: List<HabitCategoryDto> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMore: Boolean = false,
)
