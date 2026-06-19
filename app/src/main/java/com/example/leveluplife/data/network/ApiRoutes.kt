package com.example.leveluplife.data.network

object ApiRoutes {
    const val AUTH_LOGIN = "api/auth/login"
    const val AUTH_REGISTER = "api/auth/register"

    fun isAuthRequestWithoutSession(encodedPath: String): Boolean {
        val path = encodedPath.trim('/').lowercase()
        return path.endsWith(AUTH_LOGIN.lowercase()) || path.endsWith(AUTH_REGISTER.lowercase())
    }

    fun isLoginRequest(encodedPath: String): Boolean =
        encodedPath.trim('/').lowercase().endsWith(AUTH_LOGIN.lowercase())
}
