package com.example.leveluplife.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.player.ProfileCache
import com.example.leveluplife.data.player.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val habitRepository: HabitRepository,
    private val profileRepository: ProfileRepository,
    private val profileCache: ProfileCache,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadHabits()
    }

    fun refreshPlayerProgress() {
        viewModelScope.launch {
            profileCache.loadPersisted()
            profileRepository.fetchProfile()
        }
    }

    fun loadHabits() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, habits = emptyList(), currentPage = 1) }
            habitRepository.getActiveHabits(page = 1, pageSize = HABITS_PAGE_SIZE)
                .onSuccess { response ->
                    val pagination = response.pagination
                    val page = pagination?.currentPage ?: 1
                    val total = pagination?.totalPages ?: 1
                    _state.update {
                        it.copy(
                            isLoading = false,
                            habits = response.habits ?: emptyList(),
                            currentPage = page,
                            totalPages = total,
                            hasMore = page < total,
                        )
                    }
                }
                .onFailure { t ->
                    _state.update { it.copy(isLoading = false, error = t.message ?: "Error desconocido") }
                }
        }
    }

    fun loadMore() {
        val current = _state.value
        if (current.isLoadingMore || !current.hasMore) return
        val nextPage = current.currentPage + 1
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            habitRepository.getActiveHabits(page = nextPage, pageSize = HABITS_PAGE_SIZE)
                .onSuccess { response ->
                    val pagination = response.pagination
                    val page = pagination?.currentPage ?: nextPage
                    val total = pagination?.totalPages ?: current.totalPages
                    _state.update {
                        it.copy(
                            isLoadingMore = false,
                            habits = it.habits + (response.habits ?: emptyList()),
                            currentPage = page,
                            totalPages = total,
                            hasMore = page < total,
                        )
                    }
                }
                .onFailure { t ->
                    _state.update { it.copy(isLoadingMore = false, error = t.message) }
                }
        }
    }

    class Factory(
        private val habitRepository: HabitRepository,
        private val profileRepository: ProfileRepository,
        private val profileCache: ProfileCache,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(HomeViewModel::class.java))
            return HomeViewModel(habitRepository, profileRepository, profileCache) as T
        }
    }

    private companion object {
        const val HABITS_PAGE_SIZE = 50
    }
}
