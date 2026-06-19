package com.example.leveluplife.ui.evidence

import com.example.leveluplife.data.network.dto.EvidenceDto

data class EvidenceGalleryUiState(
    val isLoading: Boolean = true,
    val evidences: List<EvidenceDto> = emptyList(),
    val error: String? = null,
    val isUploading: Boolean = false,
    val uploadError: String? = null,
    val uploadSuccess: Boolean = false,
)
