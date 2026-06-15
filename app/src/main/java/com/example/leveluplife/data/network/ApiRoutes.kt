package com.example.leveluplife.data.network

object ApiRoutes {
    const val AUTH_LOGIN = "api/auth/login"

    fun isLoginRequest(encodedPath: String): Boolean =
        encodedPath.trim('/').lowercase().endsWith(AUTH_LOGIN.lowercase())
}
