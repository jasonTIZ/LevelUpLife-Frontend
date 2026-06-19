package com.example.leveluplife.ui.evidence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.habits.EvidenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EvidenceGalleryViewModel(
    private val repository: EvidenceRepository,
    private val taskId: Int,
) : ViewModel() {

    private val _state = MutableStateFlow(EvidenceGalleryUiState())
    val state: StateFlow<EvidenceGalleryUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getTaskEvidences(taskId)
                .onSuccess { evidences ->
                    _state.update { it.copy(isLoading = false, evidences = evidences) }
                }
                .onFailure { t ->
                    _state.update { it.copy(isLoading = false, error = t.message) }
                }
        }
    }

    class Factory(
        private val repository: EvidenceRepository,
        private val taskId: Int,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EvidenceGalleryViewModel(repository, taskId) as T
    }
}
