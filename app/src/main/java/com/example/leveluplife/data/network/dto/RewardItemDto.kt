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
data class PlayerInventoryDto(
    val id: Int = 0,
    val playerUserId: Int = 0,
    val rewardItemId: Int = 0,
    val rewardItemName: String = "",
    val rewardItemTypeId: Int? = null,
    val rewardItemTypeName: String? = null,
    val costGold: Double = 0.0,
    val effectValue: Double? = null,
    val quantity: Int = 1,
    val isEquipped: Boolean = false,
    val acquiredAt: String = "",
)
