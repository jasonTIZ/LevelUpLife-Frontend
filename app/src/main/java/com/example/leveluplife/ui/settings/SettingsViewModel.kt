package com.example.leveluplife.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.player.DeactivateAccountException
import com.example.leveluplife.data.player.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class SettingsViewModel(
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun onRequestDeactivate() {
        _state.update {
            it.copy(
                showConfirmDialog = true,
                consequencesAcknowledged = false,
                reason = "",
                errorMessage = null,
            )
        }
    }

    fun onCancelDeactivate() {
        _state.update {
            it.copy(
                showConfirmDialog = false,
                consequencesAcknowledged = false,
                reason = "",
                errorMessage = null,
            )
        }
    }

    fun onConsequencesAcknowledgedChange(value: Boolean) {
        _state.update { it.copy(consequencesAcknowledged = value) }
    }

    fun onReasonChange(value: String) {
        _state.update { it.copy(reason = value.take(200)) }
    }

    fun dismissError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun consumeDeactivatedEvent() {
        _state.update { it.copy(accountDeactivated = false, deactivationMessage = null) }
    }

    fun confirmDeactivate() {
        val current = _state.value
        if (!current.consequencesAcknowledged || current.isDeactivating) return

        viewModelScope.launch {
            _state.update {
                it.copy(isDeactivating = true, errorMessage = null)
            }
            playerRepository.deactivateAccount(current.reason)
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            isDeactivating = false,
                            showConfirmDialog = false,
                            accountDeactivated = true,
                            deactivationMessage = result.message,
                        )
                    }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(
                            isDeactivating = false,
                            errorMessage = mapError(t),
                        )
                    }
                }
        }
    }

    private fun mapError(t: Throwable): String = when (t) {
        is DeactivateAccountException -> when (val err = t.error) {
            is com.example.leveluplife.data.player.DeactivateAccountError.Network ->
                if (err.message == "timeout" || err.message == "unknown_host") {
                    "Sin conexión. Revisá tu red e intentá de nuevo."
                } else {
                    "Sin conexión. Revisá tu red e intentá de nuevo."
                }
            else -> err.message
        }
        is IOException -> "Sin conexión. Revisá tu red e intentá de nuevo."
        else -> t.message ?: "No se pudo desactivar la cuenta."
    }

    class Factory(
        private val playerRepository: PlayerRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SettingsViewModel::class.java))
            return SettingsViewModel(playerRepository) as T
        }
    }
}
