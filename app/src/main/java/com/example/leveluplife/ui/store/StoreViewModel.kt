package com.example.leveluplife.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.network.dto.RewardItemDto
import com.example.leveluplife.data.player.ProfileCache
import com.example.leveluplife.data.rewards.RewardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StoreViewModel(
    private val repository: RewardRepository,
    private val profileCache: ProfileCache,
) : ViewModel() {

    private val _state = MutableStateFlow(StoreUiState())
    val state: StateFlow<StoreUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getRewardItems()
                .onSuccess { items ->
                    _state.update { it.copy(isLoading = false, allItems = items) }
                }
                .onFailure { t ->
                    _state.update { it.copy(isLoading = false, error = t.message) }
                }
        }
    }

    fun onSearchChange(query: String) = _state.update { it.copy(searchQuery = query) }

    fun purchase(item: RewardItemDto) {
        if (_state.value.buyingItemId != null) return
        viewModelScope.launch {
            _state.update { it.copy(buyingItemId = item.id, buyError = null, purchaseSuccessName = null) }
            repository.purchaseItem(item.id)
                .onSuccess { response ->
                    profileCache.setGold(response.remainingGold)
                    _state.update {
                        it.copy(
                            buyingItemId = null,
                            purchaseSuccessName = response.inventory.rewardItemName,
                        )
                    }
                }
                .onFailure { t ->
                    val errorKey = when (t.message) {
                        "insufficient_gold" -> "insufficient_gold"
                        else -> "generic"
                    }
                    _state.update { it.copy(buyingItemId = null, buyError = errorKey) }
                }
        }
    }

    fun clearPurchaseSuccess() = _state.update { it.copy(purchaseSuccessName = null) }
    fun clearBuyError() = _state.update { it.copy(buyError = null) }

    class Factory(
        private val repository: RewardRepository,
        private val profileCache: ProfileCache,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(StoreViewModel::class.java))
            return StoreViewModel(repository, profileCache) as T
        }
    }
}
