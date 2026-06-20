package com.example.leveluplife.health

import kotlinx.serialization.Serializable

@Serializable
data class HealthEvidencePayload(
    val metric: String = "",
    val value: Double = 0.0,
    val unit: String = "",
    val date: String = "",
)
