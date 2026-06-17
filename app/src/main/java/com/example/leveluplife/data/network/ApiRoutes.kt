package com.example.leveluplife.data.network

object ApiRoutes {
    const val AUTH_LOGIN = "api/auth/login"
    const val AUTH_LOGOUT = "api/auth/logout"

    fun isLoginRequest(encodedPath: String): Boolean =
        isAuthExemptRequest(encodedPath) && encodedPath.trim('/').lowercase().endsWith(AUTH_LOGIN.lowercase())

    fun isAuthExemptRequest(encodedPath: String): Boolean {
        val normalized = encodedPath.trim('/').lowercase()
        return normalized.endsWith(AUTH_LOGIN.lowercase()) ||
            normalized.endsWith(AUTH_LOGOUT.lowercase())
    }
}
