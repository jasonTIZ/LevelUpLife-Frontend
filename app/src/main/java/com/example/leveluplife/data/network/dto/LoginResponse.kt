package com.example.leveluplife.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val user: UserDto,
)

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val roles: List<String> = emptyList(),
)

@Serializable
data class ApiErrorBody(
    val message: String? = null,
    val code: String? = null,
    val errors: Map<String, List<String>>? = null,
)
