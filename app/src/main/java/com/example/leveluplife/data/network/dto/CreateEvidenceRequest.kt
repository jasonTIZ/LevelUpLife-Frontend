package com.example.leveluplife.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateEvidenceRequest(
    @SerialName("url") val url: String? = null,
    @SerialName("healthDataJson") val healthDataJson: String? = null,
    @SerialName("uploadedAt") val uploadedAt: String,
)

@Serializable
data class EvidenceFileUploadResponse(
    @SerialName("url") val url: String,
)
