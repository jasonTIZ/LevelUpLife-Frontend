package com.example.leveluplife.ui.inventory

import com.example.leveluplife.data.network.dto.ActiveEffectDto
import com.example.leveluplife.data.network.dto.PlayerInventoryDto

data class InventoryUiState(
    val isLoading: Boolean = true,
    val items: List<PlayerInventoryDto> = emptyList(),
    val activeEffects: List<ActiveEffectDto> = emptyList(),
    val activatingItemId: Int? = null,
    val activateError: String? = null,
    val recoveryMessage: String? = null,
    val error: String? = null,
)
