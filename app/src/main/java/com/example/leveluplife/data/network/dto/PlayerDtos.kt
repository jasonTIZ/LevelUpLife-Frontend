package com.example.leveluplife.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeletePlayerAccountRequest(
    val reason: String? = null,
)

@Serializable
data class DeletePlayerAccountResponse(
    val success: Boolean = false,
    val message: String = "",
    val deactivatedAt: String = "",
)
