package com.example.leveluplife.ui.inventory

import com.example.leveluplife.data.network.dto.PlayerInventoryDto

data class InventoryUiState(
    val isLoading: Boolean = true,
    val items: List<PlayerInventoryDto> = emptyList(),
    val error: String? = null,
)
