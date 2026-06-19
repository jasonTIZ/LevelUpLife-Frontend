package com.example.leveluplife.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepetitionCriteriaRequestDto(
    val repetitions: Int,
    val measurementUnit: MeasurementUnit,
    @SerialName("isPartialAllowed") val isPartialAllowed: Boolean? = null,
    @SerialName("isActive") val isActive: Boolean? = null,
)
