package com.example.leveluplife.ui.evidence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.habits.EvidenceRepository
import com.example.leveluplife.data.network.dto.EvidenceDto
import com.example.leveluplife.health.HealthConnectManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EvidenceGalleryViewModel(
    private val repository: EvidenceRepository,
    private val taskId: Int,
    private val isTaskCompleted: Boolean,
    private val healthConnectManager: HealthConnectManager,
    evidenceType: String?,
) : ViewModel() {

    private val _state = MutableStateFlow(EvidenceGalleryUiState())
    val state: StateFlow<EvidenceGalleryUiState> = _state.asStateFlow()

    val isHealthConnectTask: Boolean = evidenceType == "HEALTH_CONNECT"
    val isHealthConnectAvailable: Boolean = healthConnectManager.isAvailable()
    val healthPermissions: Set<String> = healthConnectManager.permissions
    val galleryMimeType: String = if (evidenceType == "VIDEO") "video/*" else "image/*"

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
        if (isTaskCompleted) {
            _state.update { it.copy(deleteError = "task_completed") }
            return
        }
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

    fun syncHealthMetric(metric: HealthConnectManager.Metric) {
        viewModelScope.launch {
            _state.update { it.copy(isUploading = true, uploadError = null) }
            val reading = runCatching { healthConnectManager.readTodayTotal(metric) }.getOrNull()
            if (reading == null) {
                _state.update { it.copy(isUploading = false, uploadError = "health_read_failed") }
                return@launch
            }
            repository.addHealthEvidence(taskId, healthConnectManager.toHealthDataJson(reading))
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
        private val isTaskCompleted: Boolean = false,
        private val healthConnectManager: HealthConnectManager,
        private val evidenceType: String? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EvidenceGalleryViewModel(
                repository,
                taskId,
                isTaskCompleted,
                healthConnectManager,
                evidenceType,
            ) as T
    }
}
