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

interface HabitTaskRepository {
    suspend fun createHabitTask(request: CreateHabitTaskRequest): Result<HabitTaskDto>
    suspend fun getHabitTask(taskId: Int): Result<HabitTaskDto>
    suspend fun completeHabitTask(
        taskId: Int,
        completedAt: Instant = Instant.now(),
    ): Result<CompleteHabitTaskResponse>
}

class DefaultHabitTaskRepository(
    private val api: HabitTasksApi,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : HabitTaskRepository {

    override suspend fun createHabitTask(request: CreateHabitTaskRequest): Result<HabitTaskDto> =
        executeTaskCall { api.createHabitTask(request) }

    override suspend fun getHabitTask(taskId: Int): Result<HabitTaskDto> =
        executeTaskCall { api.getHabitTask(taskId) }

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
                HabitTaskCompletionFailure("Task not found or cannot be completed."),
            )
            response.code() == 403 -> Result.failure(
                HabitTaskCompletionFailure("You do not have permission to complete this task."),
            )
            response.code() == 401 -> Result.failure(
                HabitTaskCompletionFailure("Session expired. Please sign in again."),
            )
            response.code() in 500..599 -> Result.failure(
                HabitTaskCompletionFailure("Server error. Try again later."),
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

    private suspend fun executeTaskCall(
        call: suspend () -> retrofit2.Response<HabitTaskDto>,
    ): Result<HabitTaskDto> = try {
        val response = call()
        when {
            response.isSuccessful -> Result.success(requireNotNull(response.body()))
            response.code() == 409 -> Result.failure(
                Exception("This habit already has an active task. Deactivate it before creating another."),
            )
            response.code() == 404 -> Result.failure(Exception("Habit or task not found."))
            response.code() == 403 -> Result.failure(Exception("You do not have permission to modify this task."))
            response.code() == 401 -> Result.failure(Exception("Session expired. Please sign in again."))
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
        if (body.isNullOrBlank()) return "Could not complete the task."
        return runCatching {
            val envelope = json.decodeFromString<CompletionErrorEnvelope>(body)
            envelope.details?.takeIf { it.isNotBlank() }
                ?: envelope.message?.takeIf { it.isNotBlank() }
        }.getOrNull() ?: body
    }

    @kotlinx.serialization.Serializable
    private data class CompletionErrorEnvelope(
        val code: String? = null,
        val message: String? = null,
        val details: String? = null,
    )
}
