package com.example.leveluplife.data.auth

import android.util.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Helpers mínimos para extraer claims de un JWT sin verificar firma.
 *
 * El backend devuelve un JWT con claim estándar `sub` (id del usuario) y
 * `unique_name` (username). Aquí solo lo decodificamos para presentación
 * local; la firma se valida en el servidor en cada request autenticado.
 */
internal object JwtUtils {

    private val json = Json { ignoreUnknownKeys = true }

    fun extractClaim(token: String, claim: String): String? = runCatching {
        val parts = token.split('.')
        if (parts.size < 2) return@runCatching null
        val payload = String(
            Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP),
            Charsets.UTF_8,
        )
        json.parseToJsonElement(payload).jsonObject[claim]?.jsonPrimitive?.content
    }.getOrNull()

    fun extractSub(token: String): String? = extractClaim(token, "sub")
}
