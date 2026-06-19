package com.example.leveluplife.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateEvidenceRequest(
    @SerialName("DSC_EVIDENCE_PATH_URL") val url: String? = null,
    @SerialName("DSC_HEALTH_DATA_JSON") val healthDataJson: String? = null,
    @SerialName("FEC_UPLOADED") val uploadedAt: String,
)

@Serializable
data class EvidenceFileUploadResponse(
    @SerialName("url") val url: String,
)
