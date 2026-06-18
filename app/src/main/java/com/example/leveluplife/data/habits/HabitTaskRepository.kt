package com.example.leveluplife.data.habits

import com.example.leveluplife.data.network.HabitTasksApi
import com.example.leveluplife.data.network.dto.CreateHabitTaskRequest
import com.example.leveluplife.data.network.dto.HabitTaskDto
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class HabitTaskConflictFailure(
    override val message: String,
) : Exception(message)

interface HabitTaskRepository {
    suspend fun createHabitTask(request: CreateHabitTaskRequest): Result<HabitTaskDto>
    suspend fun getHabitTask(taskId: Int): Result<HabitTaskDto>
    suspend fun updateHabitTask(taskId: Int, request: CreateHabitTaskRequest): Result<HabitTaskDto>
}

class DefaultHabitTaskRepository(
    private val api: HabitTasksApi,
) : HabitTaskRepository {

    override suspend fun createHabitTask(request: CreateHabitTaskRequest): Result<HabitTaskDto> =
        execute { api.createHabitTask(request) }

    override suspend fun getHabitTask(taskId: Int): Result<HabitTaskDto> =
        execute { api.getHabitTask(taskId) }

    override suspend fun updateHabitTask(
        taskId: Int,
        request: CreateHabitTaskRequest,
    ): Result<HabitTaskDto> = execute { api.updateHabitTask(taskId, request) }

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
            response.code() == 403 -> Result.failure(Exception("No tienes permiso para modificar esta tarea."))
            response.code() == 401 -> Result.failure(Exception("Sesión expirada. Vuelve a iniciar sesión."))
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
