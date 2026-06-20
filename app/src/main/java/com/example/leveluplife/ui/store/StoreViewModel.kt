package com.example.leveluplife.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.network.dto.RewardItemDto
import com.example.leveluplife.data.rewards.PurchasedItemStorage
import com.example.leveluplife.data.rewards.RewardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StoreViewModel(
    private val repository: RewardRepository,
    private val purchasedItemStorage: PurchasedItemStorage,
) : ViewModel() {

    private val _state = MutableStateFlow(StoreUiState())
    val state: StateFlow<StoreUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getRewardItems()
                .onSuccess { items ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            allItems = items,
                            purchasedIds = purchasedItemStorage.getPurchasedIds(),
                        )
                    }
                }
                .onFailure { t ->
                    _state.update { it.copy(isLoading = false, error = t.message) }
                }
        }
    }

    fun onSearchChange(query: String) = _state.update { it.copy(searchQuery = query) }

    fun onBuyClick(item: RewardItemDto) {
        _state.update { it.copy(purchaseMessage = item.name) }
    }

    fun clearPurchaseMessage() = _state.update { it.copy(purchaseMessage = null) }

    fun onItemPurchased(itemId: Int) {
        _state.update { it.copy(purchasedIds = it.purchasedIds + itemId) }
    }

    class Factory(
        private val repository: RewardRepository,
        private val purchasedItemStorage: PurchasedItemStorage,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(StoreViewModel::class.java))
            return StoreViewModel(repository, purchasedItemStorage) as T
        }
    }
}
