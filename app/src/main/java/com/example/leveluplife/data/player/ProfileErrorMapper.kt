package com.example.leveluplife.data.player

import com.example.leveluplife.data.network.dto.ApiErrorEnvelopeDto
import com.example.leveluplife.data.network.dto.ApiProblemDetailsDto
import kotlinx.serialization.json.Json

object ProfileErrorMapper {

    fun fromHttpCode(
        code: Int,
        body: String?,
        json: Json,
    ): ProfileError {
        val envelope = body?.let { runCatching { json.decodeFromString<ApiErrorEnvelopeDto>(it) }.getOrNull() }
        val problem = body?.let { runCatching { json.decodeFromString<ApiProblemDetailsDto>(it) }.getOrNull() }

        return when (code) {
            400 -> {
                val fieldErrors = problem?.errors?.mapNotNull { (key, messages) ->
                    mapServerField(key)?.let { field -> field to (messages.firstOrNull() ?: "") }
                }?.toMap().orEmpty()

                ProfileError.Validation(
                    message = envelope?.details ?: problem?.title ?: "Invalid data.",
                    fieldErrors = fieldErrors,
                )
            }
            401 -> ProfileError.Unauthorized(
                envelope?.details ?: "Session expired. Please sign in again.",
            )
            404 -> ProfileError.NotFound(
                envelope?.details ?: "Player profile not found.",
            )
            409 -> ProfileError.Conflict(
                envelope?.details ?: "Username is already taken.",
            )
            412 -> ProfileError.PreconditionFailed(
                envelope?.details ?: "Profile was modified elsewhere. Reload and try again.",
            )
            in 500..599 -> ProfileError.Server(
                envelope?.details ?: "Server error. Try again later.",
            )
            else -> ProfileError.Unknown(
                envelope?.details ?: "Unexpected error occurred.",
            )
        }
    }

    private fun mapServerField(key: String): ProfileField? {
        val normalized = key.lowercase()
        return when {
            normalized.contains("persondata.name") || normalized.endsWith(".name") -> ProfileField.Name
            normalized.contains("persondata.lastname") || normalized.contains("lastname") -> ProfileField.LastName
            normalized.contains("persondata.email") || normalized.endsWith(".email") -> ProfileField.Email
            normalized.contains("persondata.birthdate") || normalized.contains("birthdate") -> ProfileField.Birthdate
            normalized.contains("playerdata.username") || normalized.contains("username") -> ProfileField.UserName
            else -> null
        }
    }
}
