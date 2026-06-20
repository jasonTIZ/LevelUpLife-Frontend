package com.example.leveluplife.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class RewardItemDto(
    val id: Int = 0,
    val typeId: Int? = null,
    val typeName: String? = null,
    val name: String = "",
    val description: String? = null,
    val costGold: Double = 0.0,
    val effectValue: Double? = null,
    val imageUrl: String? = null,
    val isActive: Boolean = true,
)

@Serializable
data class RewardItemFilterRequest(
    val typeId: Int? = null,
    val name: String? = null,
    val description: String? = null,
    val costGold: Double? = null,
    val effectValue: Double? = null,
)
