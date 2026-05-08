package com.example.leveluplife.data.error

import com.example.leveluplife.data.network.dto.ApiErrorBody
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object AuthErrorMapper {

    fun fromHttpCode(code: Int, rawBody: String?, json: Json? = null): AuthError {
        val message = parseMessage(rawBody, json)
        return when (code) {
            400 -> AuthError.BadRequest(message)
            401 -> AuthError.InvalidCredentials(message)
            423 -> AuthError.AccountLocked(message)
            in 500..599 -> AuthError.Server(message)
            else -> AuthError.Unknown(message ?: "HTTP $code")
        }
    }

    fun fromThrowable(t: Throwable): AuthError = when (t) {
        is SocketTimeoutException -> AuthError.Network("timeout")
        is UnknownHostException -> AuthError.Network("unknown_host")
        is IOException -> AuthError.Network(t.message)
        else -> AuthError.Unknown(t.message)
    }

    private fun parseMessage(rawBody: String?, json: Json?): String? {
        if (rawBody.isNullOrBlank()) return null
        if (json == null) return rawBody.take(240)
        return runCatching {
            val parsed = json.decodeFromString(ApiErrorBody.serializer(), rawBody)
            parsed.message ?: parsed.errors?.values?.flatten()?.firstOrNull()
        }.getOrNull() ?: rawBody.take(240)
    }
}
