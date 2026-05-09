package com.example.leveluplife.ui.disciplines

import com.example.leveluplife.data.network.dto.DisciplineData
import com.example.leveluplife.data.network.dto.HabitData

sealed class DisciplineDetailUiState {
    object Loading : DisciplineDetailUiState()
    object NotFound : DisciplineDetailUiState()
    data class Error(val message: String?) : DisciplineDetailUiState()
    data class Success(
        val discipline: DisciplineData,
        val habits: List<HabitData>,
        val currentPage: Int,
        val totalPages: Int,
        val totalItems: Int,
        val isLoadingHabits: Boolean,
        val isAdmin: Boolean,
    ) : DisciplineDetailUiState()
}
