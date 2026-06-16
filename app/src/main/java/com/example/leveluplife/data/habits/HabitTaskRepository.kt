package com.example.leveluplife.data.habits

import com.example.leveluplife.data.network.HabitTasksApi
import com.example.leveluplife.data.network.dto.CreateHabitTaskRequest
import com.example.leveluplife.data.network.dto.HabitTaskDto
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

interface HabitTaskRepository {
    suspend fun createHabitTask(request: CreateHabitTaskRequest): Result<HabitTaskDto>
}

class DefaultHabitTaskRepository(
    private val api: HabitTasksApi,
) : HabitTaskRepository {

    override suspend fun createHabitTask(request: CreateHabitTaskRequest): Result<HabitTaskDto> = try {
        val response = api.createHabitTask(request)
        when {
            response.isSuccessful -> Result.success(requireNotNull(response.body()))
            response.code() == 409 -> Result.failure(
                Exception("Este hábito ya tiene una tarea activa. Desactívala antes de crear otra."),
            )
            response.code() == 404 -> Result.failure(Exception("Hábito no encontrado o inactivo."))
            response.code() == 403 -> Result.failure(Exception("No tienes permiso para crear tareas en este hábito."))
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
