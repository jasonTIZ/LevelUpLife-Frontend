package com.example.leveluplife.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.rewards.RewardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val repository: RewardRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(InventoryUiState())
    val state: StateFlow<InventoryUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getInventory()
                .onSuccess { response ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            items = response.items,
                            activeEffects = response.activeEffects,
                        )
                    }
                }
                .onFailure { t ->
                    _state.update { it.copy(isLoading = false, error = t.message) }
                }
        }
    }

    fun activate(inventoryId: Int) {
        if (_state.value.activatingItemId != null) return
        viewModelScope.launch {
            _state.update {
                it.copy(activatingItemId = inventoryId, activateError = null, recoveryMessage = null)
            }
            repository.activateItem(inventoryId)
                .onSuccess { response ->
                    _state.update {
                        it.copy(
                            activatingItemId = null,
                            recoveryMessage = response.recoveryMessage,
                        )
                    }
                    load()
                }
                .onFailure { t ->
                    val errorKey = when (t.message) {
                        "no_quantity" -> "no_quantity"
                        "effect_already_active" -> "effect_already_active"
                        "recovery_no_target" -> "recovery_no_target"
                        else -> "generic"
                    }
                    _state.update { it.copy(activatingItemId = null, activateError = errorKey) }
                }
        }
    }

    fun clearActivateError() = _state.update { it.copy(activateError = null) }
    fun clearRecoveryMessage() = _state.update { it.copy(recoveryMessage = null) }

    class Factory(
        private val repository: RewardRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(InventoryViewModel::class.java))
            return InventoryViewModel(repository) as T
        }
    }
}
