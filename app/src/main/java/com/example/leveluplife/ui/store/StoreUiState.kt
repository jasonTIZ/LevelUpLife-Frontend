package com.example.leveluplife.ui.store

import com.example.leveluplife.data.network.dto.RewardItemDto

data class StoreUiState(
    val isLoading: Boolean = true,
    val allItems: List<RewardItemDto> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val purchasedIds: Set<Int> = emptySet(),
    val purchaseMessage: String? = null,
) {
    val filteredItems: List<RewardItemDto>
        get() {
            val q = searchQuery.trim()
            return allItems.filter { q.isBlank() || it.name.contains(q, ignoreCase = true) }
        }

    // Preserves backend order; groups appear in the order their first item arrives
    val groupedItems: Map<String, List<RewardItemDto>>
        get() = filteredItems.groupBy { it.typeName ?: "Otros" }
}
