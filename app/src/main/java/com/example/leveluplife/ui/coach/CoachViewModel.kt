package com.example.leveluplife.ui.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.auth.TokenStore
import com.example.leveluplife.data.coach.ChatMessage
import com.example.leveluplife.data.coach.ChatStorage
import com.example.leveluplife.data.coach.CoachRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CoachViewModel(
    private val repository: CoachRepository,
    private val tokenStore: TokenStore,
    private val storage: ChatStorage,
) : ViewModel() {

    private val _state = MutableStateFlow(CoachUiState())
    val state: StateFlow<CoachUiState> = _state.asStateFlow()

    init {
        val saved = storage.load()
        if (saved.isNotEmpty()) {
            _state.update { it.copy(messages = saved) }
        }
    }

    fun onInputChange(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isBlank() || _state.value.isLoading) return

        val username = tokenStore.userId() ?: "anonymous"
        val userMsg = ChatMessage(text = text, isFromUser = true)

        _state.update { state ->
            state.copy(
                messages = state.messages + userMsg,
                inputText = "",
                isLoading = true,
                error = null,
            )
        }

        viewModelScope.launch {
            repository.sendMessage(username, text)
                .onSuccess { reply ->
                    val aiMsg = ChatMessage(text = reply, isFromUser = false)
                    _state.update { state ->
                        val updated = state.copy(
                            messages = state.messages + aiMsg,
                            isLoading = false,
                        )
                        storage.save(updated.messages)
                        updated
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                    storage.save(_state.value.messages)
                }
        }
    }

    fun clearHistory() {
        storage.clear()
        _state.update { CoachUiState() }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    class Factory(
        private val repository: CoachRepository,
        private val tokenStore: TokenStore,
        private val storage: ChatStorage,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CoachViewModel(repository, tokenStore, storage) as T
    }
}
