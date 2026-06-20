package com.example.leveluplife.data.coach

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
)
