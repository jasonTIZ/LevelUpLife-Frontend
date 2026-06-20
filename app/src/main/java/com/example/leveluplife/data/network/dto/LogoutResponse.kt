package com.example.leveluplife.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class LogoutResponse(
    val success: Boolean = false,
    val message: String? = null,
)
