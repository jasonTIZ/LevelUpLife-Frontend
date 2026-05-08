package com.example.leveluplife.data.auth

import com.example.leveluplife.data.network.dto.LoginData

data class AuthSession(
    val accessToken: String,
    val refreshToken: String?,
    val user: AuthUser,
)

data class AuthUser(
    val id: String,
    val userName: String,
    val level: Int,
    val className: String,
) {
    companion object {
        fun fromData(data: LoginData, id: String): AuthUser = AuthUser(
            id = id,
            userName = data.userName,
            level = data.level,
            className = data.className,
        )
    }
}
