package com.example.leveluplife.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.network.dto.RewardItemDto
import com.example.leveluplife.data.rewards.RewardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StoreDetailUiState(
    val isPurchasing: Boolean = false,
    val purchaseSuccess: Boolean = false,
    val purchaseError: String? = null,
)

class StoreDetailViewModel(
    private val repository: RewardRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(StoreDetailUiState())
    val state: StateFlow<StoreDetailUiState> = _state.asStateFlow()

    fun purchase(item: RewardItemDto) {
        if (_state.value.isPurchasing) return
        viewModelScope.launch {
            _state.update { it.copy(isPurchasing = true, purchaseError = null, purchaseSuccess = false) }
            repository.purchaseItem(item.id)
                .onSuccess { response ->
                    _state.update { it.copy(isPurchasing = false, purchaseSuccess = true) }
                }
                .onFailure { t ->
                    val errorKey = when (t.message) {
                        "insufficient_gold" -> "insufficient_gold"
                        else -> "generic"
                    }
                    _state.update { it.copy(isPurchasing = false, purchaseError = errorKey) }
                }
        }
    }

    fun clearPurchaseResult() = _state.update { it.copy(purchaseSuccess = false, purchaseError = null) }

    class Factory(
        private val repository: RewardRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(StoreDetailViewModel::class.java))
            return StoreDetailViewModel(repository) as T
        }
    }
}
