package com.example.leveluplife.data.network.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class RewardItemDto(
    val id: Int = 0,
    val typeId: Int? = null,
    val typeName: String? = null,
    val name: String = "",
    val description: String? = null,
    val costGold: Double = 0.0,
    val effectValue: Double? = null,
    val durationDays: Int? = null,
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
    val durationDays: Int? = null,
    val quantity: Int = 1,
    val isEquipped: Boolean = false,
    val acquiredAt: String = "",
)

@Serializable
data class ActiveEffectDto(
    val id: Int = 0,
    val inventoryId: Int = 0,
    val rewardItemId: Int = 0,
    val rewardItemName: String = "",
    val rewardItemTypeId: Int? = null,
    val rewardItemTypeName: String? = null,
    val effectValue: Double? = null,
    val remainingCharges: Int? = null,
    val activatedAt: String = "",
    val expiresAt: String? = null,
    val isActive: Boolean = true,
)

@Serializable
data class InventoryResponseDto(
    @JsonNames("items", "Items") val items: List<PlayerInventoryDto> = emptyList(),
    @JsonNames("activeEffects", "ActiveEffects") val activeEffects: List<ActiveEffectDto> = emptyList(),
)

@Serializable
data class PurchaseResponseDto(
    val inventory: PlayerInventoryDto,
    val remainingGold: Int = 0,
)

@Serializable
data class ActivateItemResponseDto(
    val inventory: PlayerInventoryDto,
    val activeEffect: ActiveEffectDto? = null,
    val recoveryMessage: String? = null,
)
