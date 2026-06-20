package com.example.leveluplife.ui.home

import com.example.leveluplife.data.network.dto.HabitDto

data class HomeUiState(
    val habits: List<HabitDto> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMore: Boolean = false,
    val deletingHabitId: Int? = null,
    val deleteError: String? = null,
)
