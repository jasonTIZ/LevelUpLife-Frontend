package com.example.leveluplife.data.habits

import com.example.leveluplife.data.network.HabitTasksApi
import com.example.leveluplife.data.network.dto.CreateHabitTaskRequest
import com.example.leveluplife.data.network.dto.HabitTaskDto
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

interface HabitTaskRepository {
    suspend fun createHabitTask(request: CreateHabitTaskRequest): Result<HabitTaskDto>
    suspend fun getHabitTask(taskId: Int): Result<HabitTaskDto>
    suspend fun deactivateHabitTask(taskId: Int): Result<String>
}

class DefaultHabitTaskRepository(
    private val api: HabitTasksApi,
) : HabitTaskRepository {

    override suspend fun createHabitTask(request: CreateHabitTaskRequest): Result<HabitTaskDto> =
        executeTaskCall { api.createHabitTask(request) }

    override suspend fun getHabitTask(taskId: Int): Result<HabitTaskDto> =
        executeTaskCall { api.getHabitTask(taskId) }

    override suspend fun deactivateHabitTask(taskId: Int): Result<String> = try {
        val response = api.deactivateHabitTask(taskId)
        when {
            response.isSuccessful -> Result.success(
                response.body()?.message?.ifBlank { null }
                    ?: "Task deactivated successfully",
            )
            response.code() == 404 -> Result.failure(Exception("Task not found or already removed."))
            response.code() == 403 -> Result.failure(Exception("You do not have permission to deactivate this task."))
            response.code() == 401 -> Result.failure(Exception("Session expired. Please sign in again."))
            response.code() in 500..599 -> Result.failure(Exception("Server error. Try again later."))
            else -> Result.failure(Exception("HTTP ${response.code()}"))
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
}
