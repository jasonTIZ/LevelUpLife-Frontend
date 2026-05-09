package com.example.leveluplife.ui.disciplines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.auth.JwtUtils
import com.example.leveluplife.data.auth.TokenStore
import com.example.leveluplife.data.disciplines.DisciplineError
import com.example.leveluplife.data.disciplines.DisciplineErrorException
import com.example.leveluplife.data.disciplines.DisciplineRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 10

class DisciplineDetailViewModel(
    private val disciplineId: String,
    private val repository: DisciplineRepository,
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _state = MutableStateFlow<DisciplineDetailUiState>(DisciplineDetailUiState.Loading)
    val state: StateFlow<DisciplineDetailUiState> = _state.asStateFlow()

    init {
        load(page = 1)
    }

    fun load(page: Int = 1) {
        viewModelScope.launch {
            _state.value = DisciplineDetailUiState.Loading

            val disciplineDeferred = async { repository.getDiscipline(disciplineId) }
            val habitsDeferred = async { repository.getHabits(disciplineId, page, PAGE_SIZE) }

            val disciplineResult = disciplineDeferred.await()
            if (disciplineResult.isFailure) {
                val err = (disciplineResult.exceptionOrNull() as? DisciplineErrorException)?.disciplineError
                _state.value = if (err is DisciplineError.NotFound) {
                    DisciplineDetailUiState.NotFound
                } else {
                    DisciplineDetailUiState.Error(err?.message)
                }
                return@launch
            }

            val discipline = disciplineResult.getOrThrow()
            val isAdmin = tokenStore.accessToken()?.let {
                JwtUtils.extractClaim(it, "role")?.lowercase() == "admin"
            } ?: false

            val habitsResult = habitsDeferred.await()
            val paged = habitsResult.getOrNull()
            _state.value = DisciplineDetailUiState.Success(
                discipline = discipline,
                habits = paged?.habits ?: emptyList(),
                currentPage = paged?.currentPage ?: page,
                totalPages = paged?.totalPages ?: 1,
                totalItems = paged?.totalItems ?: 0,
                isLoadingHabits = false,
                isAdmin = isAdmin,
            )
        }
    }

    fun loadPage(page: Int) {
        val current = _state.value as? DisciplineDetailUiState.Success ?: return
        _state.value = current.copy(isLoadingHabits = true)
        viewModelScope.launch {
            val result = repository.getHabits(disciplineId, page, PAGE_SIZE)
            val paged = result.getOrNull()
            _state.value = current.copy(
                habits = paged?.habits ?: current.habits,
                currentPage = paged?.currentPage ?: page,
                totalPages = paged?.totalPages ?: current.totalPages,
                totalItems = paged?.totalItems ?: current.totalItems,
                isLoadingHabits = false,
            )
        }
    }

    class Factory(
        private val disciplineId: String,
        private val repository: DisciplineRepository,
        private val tokenStore: TokenStore,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(DisciplineDetailViewModel::class.java))
            return DisciplineDetailViewModel(disciplineId, repository, tokenStore) as T
        }
    }
}
