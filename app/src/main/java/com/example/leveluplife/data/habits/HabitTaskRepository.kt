package com.example.leveluplife.data.habits

import com.example.leveluplife.data.network.HabitTasksApi
import com.example.leveluplife.data.network.dto.CompleteHabitTaskRequest
import com.example.leveluplife.data.network.dto.CompleteHabitTaskResponse
import com.example.leveluplife.data.network.dto.CreateHabitTaskRequest
import com.example.leveluplife.data.network.dto.HabitTaskDto
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Instant

class HabitTaskCompletionFailure(
    override val message: String,
) : Exception(message)

class HabitTaskConflictFailure(
    override val message: String,
) : Exception(message)

interface HabitTaskRepository {
    suspend fun createHabitTask(request: CreateHabitTaskRequest): Result<HabitTaskDto>
    suspend fun getHabitTask(taskId: Int): Result<HabitTaskDto>
    suspend fun completeHabitTask(
        taskId: Int,
        completedAt: Instant = Instant.now(),
    ): Result<CompleteHabitTaskResponse>
    suspend fun updateHabitTask(taskId: Int, request: CreateHabitTaskRequest): Result<HabitTaskDto>
    suspend fun deactivateHabitTask(taskId: Int): Result<String>
}

class DefaultHabitTaskRepository(
    private val api: HabitTasksApi,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : HabitTaskRepository {

    override suspend fun createHabitTask(request: CreateHabitTaskRequest): Result<HabitTaskDto> =
        execute { api.createHabitTask(request) }

    override suspend fun getHabitTask(taskId: Int): Result<HabitTaskDto> =
        execute { api.getHabitTask(taskId) }

    override suspend fun completeHabitTask(
        taskId: Int,
        completedAt: Instant,
    ): Result<CompleteHabitTaskResponse> = try {
        val response = api.completeHabitTask(
            taskId = taskId,
            body = CompleteHabitTaskRequest(completedAt = completedAt.toString()),
        )
        when {
            response.isSuccessful -> Result.success(requireNotNull(response.body()))
            response.code() == 400 -> Result.failure(
                HabitTaskCompletionFailure(parseCompletionError(response.errorBody()?.string())),
            )
            response.code() == 404 -> Result.failure(
                HabitTaskCompletionFailure("Tarea no encontrada o no se puede completar."),
            )
            response.code() == 403 -> Result.failure(
                HabitTaskCompletionFailure("No tenés permiso para completar esta tarea."),
            )
            response.code() == 401 -> Result.failure(
                HabitTaskCompletionFailure("Sesión expirada. Volvé a iniciar sesión."),
            )
            response.code() in 500..599 -> Result.failure(
                HabitTaskCompletionFailure("Error del servidor. Intentá más tarde."),
            )
            else -> Result.failure(HabitTaskCompletionFailure("HTTP ${response.code()}"))
        }
    } catch (e: SocketTimeoutException) {
        Result.failure(IOException("timeout", e))
    } catch (e: UnknownHostException) {
        Result.failure(IOException("unknown_host", e))
    } catch (t: Throwable) {
        Result.failure(t)
    }

    override suspend fun updateHabitTask(
        taskId: Int,
        request: CreateHabitTaskRequest,
    ): Result<HabitTaskDto> = execute { api.updateHabitTask(taskId, request) }

    override suspend fun deactivateHabitTask(taskId: Int): Result<String> = try {
        val response = api.deactivateHabitTask(taskId)
        when {
            response.isSuccessful -> Result.success(
                response.body()?.message?.ifBlank { null }
                    ?: "Tarea desactivada correctamente.",
            )
            response.code() == 404 -> Result.failure(Exception("Tarea no encontrada o ya inactiva."))
            response.code() == 403 -> Result.failure(Exception("No tenés permiso para desactivar esta tarea."))
            response.code() == 401 -> Result.failure(Exception("Sesión expirada. Volvé a iniciar sesión."))
            response.code() in 500..599 -> Result.failure(Exception("Error del servidor. Intentá más tarde."))
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (e: SocketTimeoutException) {
        Result.failure(IOException("timeout", e))
    } catch (e: UnknownHostException) {
        Result.failure(IOException("unknown_host", e))
    } catch (t: Throwable) {
        Result.failure(t)
    }

    private suspend fun execute(
        call: suspend () -> retrofit2.Response<HabitTaskDto>,
    ): Result<HabitTaskDto> = try {
        val response = call()
        when {
            response.isSuccessful -> Result.success(requireNotNull(response.body()))
            response.code() == 409 -> Result.failure(
                HabitTaskConflictFailure(
                    "La tarea cambió en otro lugar. Recargá los datos e intentá de nuevo.",
                ),
            )
            response.code() == 404 -> Result.failure(Exception("Tarea no encontrada o inactiva."))
            response.code() == 403 -> Result.failure(Exception("No tenés permiso para modificar esta tarea."))
            response.code() == 401 -> Result.failure(Exception("Sesión expirada. Volvé a iniciar sesión."))
            response.code() == 400 -> {
                val body = runCatching { response.errorBody()?.string() }.getOrNull()
                val parsed = HabitTaskApiErrorParser.parse400(body)
                if (parsed.fieldErrors.hasErrors) {
                    Result.failure(
                        HabitTaskValidationFailure(parsed.fieldErrors, parsed.summary),
                    )
                } else {
                    Result.failure(Exception(parsed.summary))
                }
            }
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (e: SocketTimeoutException) {
        Result.failure(IOException("timeout", e))
    } catch (e: UnknownHostException) {
        Result.failure(IOException("unknown_host", e))
    } catch (t: Throwable) {
        Result.failure(t)
    }

    private fun parseCompletionError(body: String?): String {
        if (body.isNullOrBlank()) return "No se pudo completar la tarea."
        val raw = runCatching {
            val envelope = json.decodeFromString<CompletionErrorEnvelope>(body)
            envelope.details?.takeIf { it.isNotBlank() }
                ?: envelope.message?.takeIf { it.isNotBlank() }
        }.getOrNull() ?: body
        return localizeCompletionError(raw)
    }

    private fun localizeCompletionError(message: String): String =
        COMPLETION_ERROR_TRANSLATIONS[message.trim()] ?: message

    private companion object {
        val COMPLETION_ERROR_TRANSLATIONS = mapOf(
            "The completion date cannot be earlier than the task start date." to
                "La fecha de completado no puede ser anterior a la fecha de inicio de la tarea.",
            "Evidence must be uploaded before completing a task with EVIDENCE criteria." to
                "Debés subir evidencia antes de completar una tarea con criterio de evidencia.",
            "The habit task is inactive and cannot be completed." to
                "La tarea está inactiva y no se puede completar.",
            "The habit task has already been completed." to
                "Esta tarea ya fue completada.",
            "Active repetition criteria are required before completing this task." to
                "Se requieren criterios de repetición activos antes de completar esta tarea.",
            "Active timer criteria are required before completing this task." to
                "Se requieren criterios de temporizador activos antes de completar esta tarea.",
        )
    }

    @kotlinx.serialization.Serializable
    private data class CompletionErrorEnvelope(
        val code: String? = null,
        val message: String? = null,
        val details: String? = null,
    )
}
