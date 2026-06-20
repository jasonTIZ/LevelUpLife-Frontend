package com.example.leveluplife.ui.coach

import com.example.leveluplife.data.coach.ChatMessage

data class CoachUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val inputText: String = "",
)
