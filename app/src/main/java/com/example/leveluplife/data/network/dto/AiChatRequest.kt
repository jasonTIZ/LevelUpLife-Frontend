package com.example.leveluplife.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class AiChatRequest(
    val username: String,
    val message: String,
    val systemPrompt: String? = null,
    val model: String? = null,
)

@Serializable
data class AiChatResponse(
    val reply: String,
    val sessionId: String? = null,
)
