package com.example.leveluplife.ui.evidence

import com.example.leveluplife.data.network.dto.EvidenceDto

data class EvidenceGalleryUiState(
    val isLoading: Boolean = true,
    val evidences: List<EvidenceDto> = emptyList(),
    val error: String? = null,
    val pendingDeleteEvidence: EvidenceDto? = null,
    val deleteAcknowledged: Boolean = false,
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
    val deleteSuccess: Boolean = false,
)
