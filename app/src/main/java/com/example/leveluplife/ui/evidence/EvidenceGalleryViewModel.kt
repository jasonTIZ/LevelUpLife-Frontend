package com.example.leveluplife.ui.evidence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.habits.EvidenceRepository
import com.example.leveluplife.data.network.dto.EvidenceDto
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

    fun requestDelete(evidence: EvidenceDto) {
        _state.update { it.copy(pendingDeleteEvidence = evidence, deleteAcknowledged = false) }
    }

    fun cancelDelete() {
        _state.update { it.copy(pendingDeleteEvidence = null, deleteAcknowledged = false) }
    }

    fun onDeleteAcknowledgedChange(acknowledged: Boolean) {
        _state.update { it.copy(deleteAcknowledged = acknowledged) }
    }

    fun confirmDelete() {
        val evidence = _state.value.pendingDeleteEvidence ?: return
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }
            repository.deleteEvidence(taskId, evidence.id)
                .onSuccess {
                    _state.update { state ->
                        state.copy(
                            isDeleting = false,
                            pendingDeleteEvidence = null,
                            deleteAcknowledged = false,
                            evidences = state.evidences.filter { e -> e.id != evidence.id },
                            deleteSuccess = true,
                        )
                    }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(
                            isDeleting = false,
                            deleteError = t.message,
                        )
                    }
                }
        }
    }

    fun dismissDeleteError() {
        _state.update { it.copy(deleteError = null) }
    }

    fun onDeleteSuccessShown() {
        _state.update { it.copy(deleteSuccess = false) }
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
