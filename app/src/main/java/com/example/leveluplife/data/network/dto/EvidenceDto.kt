package com.example.leveluplife.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EvidenceDto(
    @SerialName("ID_EVIDENCE") val id: Int,
    @SerialName("ID_HABIT_TASK") val taskId: Int,
    @SerialName("DSC_EVIDENCE_PATH_URL") val url: String,
    @SerialName("DSC_HEALTH_DATA_JSON") val healthDataJson: String? = null,
    @SerialName("FEC_UPLOADED") val uploadedAt: String,
)
