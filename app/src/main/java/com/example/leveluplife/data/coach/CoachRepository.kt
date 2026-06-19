package com.example.leveluplife.data.coach

import com.example.leveluplife.data.network.AiApi
import com.example.leveluplife.data.network.dto.AiChatRequest

private const val SYSTEM_PROMPT =
    "Eres un coach de habitos personales. Tu mision es guiar al usuario para que mejore " +
    "y mantenga habitos de vida saludables, ya sea en ejercicio, alimentacion, sueno, " +
    "productividad o bienestar mental. Responde siempre en el mismo idioma del usuario, " +
    "con un tono motivador, claro y practico. Ofrece consejos concretos, formula preguntas " +
    "para entender mejor las necesidades del usuario y celebra sus logros. Adapta siempre " +
    "tus sugerencias al contexto especifico que el usuario te comparte."

private const val MODEL = "gemma2:9b"

interface CoachRepository {
    suspend fun sendMessage(username: String, message: String): Result<String>
}

class DefaultCoachRepository(private val api: AiApi) : CoachRepository {

    override suspend fun sendMessage(username: String, message: String): Result<String> = try {
        val response = api.chat(
            AiChatRequest(
                username = username,
                message = message,
                systemPrompt = SYSTEM_PROMPT,
                model = MODEL,
            )
        )
        when {
            response.isSuccessful -> Result.success(response.body()?.reply ?: "")
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
