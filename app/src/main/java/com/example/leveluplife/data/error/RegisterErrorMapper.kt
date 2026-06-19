package com.example.leveluplife.data.error

import com.example.leveluplife.data.network.dto.ApiErrorBody
import com.example.leveluplife.domain.validation.RegisterFieldKey
import kotlinx.serialization.json.Json

object RegisterErrorMapper {

    fun fieldErrorsFrom400(rawBody: String?, json: Json): Map<RegisterFieldKey, String> {
        if (rawBody.isNullOrBlank()) return emptyMap()
        val parsed = runCatching {
            json.decodeFromString(ApiErrorBody.serializer(), rawBody)
        }.getOrNull() ?: return emptyMap()

        return parsed.errors?.mapNotNull { (key, messages) ->
            mapServerField(key)?.let { field ->
                field to (messages.firstOrNull()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null)
            }
        }?.toMap().orEmpty()
    }

    fun duplicateAccountField(message: String?): RegisterFieldKey? {
        val normalized = message?.lowercase().orEmpty()
        return when {
            normalized.contains("email") -> RegisterFieldKey.EMAIL
            normalized.contains("username") || normalized.contains("usuario") -> RegisterFieldKey.USER_NAME
            else -> null
        }
    }

    private fun mapServerField(key: String): RegisterFieldKey? {
        val normalized = key.lowercase()
        return when {
            normalized.contains("persondata.name") -> RegisterFieldKey.NAME
            normalized.contains("persondata.lastname") || normalized.contains("lastname") -> RegisterFieldKey.LAST_NAME
            normalized.contains("persondata.email") -> RegisterFieldKey.EMAIL
            normalized.contains("persondata.birthdate") || normalized.contains("birthdate") -> RegisterFieldKey.BIRTHDATE
            normalized.contains("playeruserdata.username") || normalized.contains("username") -> RegisterFieldKey.USER_NAME
            normalized.contains("playeruserdata.password") || normalized.endsWith(".password") -> RegisterFieldKey.PASSWORD
            normalized.contains("playeruserdata.classid") || normalized.contains("classid") -> RegisterFieldKey.CLASS_ID
            else -> null
        }
    }
}
