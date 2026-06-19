package com.example.leveluplife.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EvidenceDto(
    @SerialName("id") val id: Int,
    @SerialName("habitTaskId") val taskId: Int,
    @SerialName("url") val url: String,
    @SerialName("healthDataJson") val healthDataJson: String? = null,
    @SerialName("uploadedAt") val uploadedAt: String,
)
