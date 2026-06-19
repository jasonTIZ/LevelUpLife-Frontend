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

    fun uploadEvidence(fileUri: String, mimeType: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true, uploadError = null) }
            repository.uploadEvidence(taskId, fileUri, mimeType)
                .onSuccess { evidence ->
                    _state.update { state ->
                        state.copy(
                            isUploading = false,
                            evidences = listOf(evidence) + state.evidences,
                            uploadSuccess = true,
                        )
                    }
                }
                .onFailure { t ->
                    _state.update { it.copy(isUploading = false, uploadError = t.message) }
                }
        }
    }

    fun onUploadSuccessShown() {
        _state.update { it.copy(uploadSuccess = false) }
    }

    fun onUploadErrorShown() {
        _state.update { it.copy(uploadError = null) }
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
