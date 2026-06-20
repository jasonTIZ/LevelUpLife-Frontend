package com.example.leveluplife.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.categories.HabitCategoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val categoryRepository: HabitCategoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CategoriesUiState())
    val state: StateFlow<CategoriesUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            loadCategories()
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, error = null, categories = emptyList(), currentPage = 1)
            }
            categoryRepository.getActiveCategories(page = 1, search = currentSearch())
                .onSuccess { response ->
                    val pagination = response.resolvedPagination
                    val page = pagination?.currentPage ?: 1
                    val total = pagination?.totalPages ?: 1
                    _state.update {
                        it.copy(
                            isLoading = false,
                            categories = response.categories,
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
            categoryRepository.getActiveCategories(page = nextPage, search = currentSearch())
                .onSuccess { response ->
                    val pagination = response.resolvedPagination
                    val page = pagination?.currentPage ?: nextPage
                    val total = pagination?.totalPages ?: current.totalPages
                    _state.update {
                        it.copy(
                            isLoadingMore = false,
                            categories = it.categories + response.categories,
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

    private fun currentSearch(): String? = _state.value.searchQuery.trim().ifBlank { null }

    class Factory(
        private val categoryRepository: HabitCategoryRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(CategoriesViewModel::class.java))
            return CategoriesViewModel(categoryRepository) as T
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
    }
}
