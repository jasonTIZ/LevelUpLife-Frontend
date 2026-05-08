package com.example.leveluplife.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("userNameOrEmail")
    val userNameOrEmail: String,
    @SerialName("password")
    val password: String,
)
