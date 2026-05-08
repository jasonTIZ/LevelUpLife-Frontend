package com.example.leveluplife.data.network.dto

import kotlinx.serialization.Serializable

/**
 * Wrapper que devuelve el backend `LevelUpLife`:
 *
 * ```
 * { "success": true, "data": { "token": "...", "userName": "...", "level": 1, "className": "..." } }
 * ```
 */
@Serializable
data class LoginResponse(
    val success: Boolean = false,
    val data: LoginData? = null,
    val message: String? = null,
)

@Serializable
data class LoginData(
    val token: String,
    val userName: String,
    val level: Int = 0,
    val className: String = "",
)

@Serializable
data class ApiErrorBody(
    val message: String? = null,
    val title: String? = null,
    val code: String? = null,
    val errors: Map<String, List<String>>? = null,
)
