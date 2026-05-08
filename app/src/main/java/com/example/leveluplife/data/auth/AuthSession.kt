package com.example.leveluplife.data.auth

import com.example.leveluplife.data.network.dto.UserDto

data class AuthSession(
    val accessToken: String,
    val refreshToken: String?,
    val user: AuthUser,
)

data class AuthUser(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val roles: List<String>,
) {
    companion object {
        fun fromDto(dto: UserDto): AuthUser = AuthUser(
            id = dto.id,
            name = dto.name,
            email = dto.email,
            avatarUrl = dto.avatarUrl,
            roles = dto.roles,
        )
    }
}
