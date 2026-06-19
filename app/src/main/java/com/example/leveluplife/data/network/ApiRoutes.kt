package com.example.leveluplife.data.network

object ApiRoutes {
    const val AUTH_LOGIN = "api/auth/login"
    const val AUTH_LOGOUT = "api/auth/logout"
    const val AUTH_REGISTER = "api/auth/register"

    fun isAuthRequestWithoutSession(encodedPath: String): Boolean = isAuthExemptRequest(encodedPath)

    fun isLoginRequest(encodedPath: String): Boolean =
        encodedPath.trim('/').lowercase().endsWith(AUTH_LOGIN.lowercase())

    fun isAuthExemptRequest(encodedPath: String): Boolean {
        val normalized = encodedPath.trim('/').lowercase()
        return normalized.endsWith(AUTH_LOGIN.lowercase()) ||
            normalized.endsWith(AUTH_LOGOUT.lowercase()) ||
            normalized.endsWith(AUTH_REGISTER.lowercase())
    }
}
