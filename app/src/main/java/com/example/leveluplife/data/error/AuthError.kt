package com.example.leveluplife.data.error

sealed class AuthError(open val message: String? = null) {
    data class InvalidCredentials(override val message: String? = null) : AuthError(message)
    data class AccountLocked(override val message: String? = null) : AuthError(message)
    data class BadRequest(override val message: String? = null) : AuthError(message)
    data class RateLimited(override val message: String? = null) : AuthError(message)
    data class Server(override val message: String? = null) : AuthError(message)
    data class Network(override val message: String? = null) : AuthError(message)
    data class Unknown(override val message: String? = null) : AuthError(message)
}
