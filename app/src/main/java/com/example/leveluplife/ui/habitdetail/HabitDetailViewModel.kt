package com.example.leveluplife.ui.habitdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.habits.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HabitDetailViewModel(
    private val habitRepository: HabitRepository,
    private val habitId: Int,
) : ViewModel() {

    private val _state = MutableStateFlow(HabitDetailUiState())
    val state: StateFlow<HabitDetailUiState> = _state.asStateFlow()

    init {
        loadHabit()
    }

    fun loadHabit() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            habitRepository.getHabitById(habitId)
                .onSuccess { habit ->
                    _state.update { it.copy(isLoading = false, habit = habit) }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(isLoading = false, error = t.message ?: "Error desconocido")
                    }
                }
        }
    }

    class Factory(
        private val habitRepository: HabitRepository,
        private val habitId: Int,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(HabitDetailViewModel::class.java))
            return HabitDetailViewModel(habitRepository, habitId) as T
        }
    }
}
