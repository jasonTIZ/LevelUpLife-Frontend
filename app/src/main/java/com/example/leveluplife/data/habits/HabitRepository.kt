package com.example.leveluplife.data.habits

import com.example.leveluplife.data.auth.TokenStore
import com.example.leveluplife.data.network.HabitsApi
import com.example.leveluplife.data.network.dto.CreateHabitRequestDto
import com.example.leveluplife.data.network.dto.CreateHabitResponseDto
import com.example.leveluplife.data.network.dto.HabitDto
import com.example.leveluplife.data.network.dto.HabitsPageResponse
import com.example.leveluplife.data.network.dto.PaginationDto
import com.example.leveluplife.data.network.dto.UpdateHabitRequestDto

interface HabitRepository {
    suspend fun getActiveHabits(page: Int, pageSize: Int = 10): Result<HabitsPageResponse>
    suspend fun createHabit(request: CreateHabitRequestDto): Result<CreateHabitResponseDto>
    suspend fun updateHabit(request: UpdateHabitRequestDto): Result<CreateHabitResponseDto>
    suspend fun deleteHabit(habitId: Int): Result<Unit>
    suspend fun getHabitById(id: Int): Result<HabitDto>
    fun setCurrentUserId(userId: Int)
    fun getCurrentUserId(): Int
}

class DefaultHabitRepository(
    private val api: HabitsApi,
    private val tokenStore: TokenStore,
) : HabitRepository {

    private var explicitUserId: Int? = null

    override fun setCurrentUserId(userId: Int) {
        explicitUserId = userId
    }

    override fun getCurrentUserId(): Int =
        tokenStore.userId()?.toIntOrNull()
            ?: explicitUserId
            ?: 1

    override suspend fun getActiveHabits(page: Int, pageSize: Int): Result<HabitsPageResponse> = try {
        val response = api.getActiveHabits(page, pageSize)
        when {
            response.isSuccessful -> Result.success(requireNotNull(response.body()))
            response.code() == 400 -> Result.success(
                HabitsPageResponse(
                    success = true,
                    habits = emptyList(),
                    pagination = PaginationDto(
                        currentPage = page,
                        pageSize = pageSize,
                        totalPages = 1,
                        totalRecords = 0,
                    ),
                )
            )
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }

    override suspend fun createHabit(request: CreateHabitRequestDto): Result<CreateHabitResponseDto> = try {
        val response = api.createHabit(request)
        when {
            response.isSuccessful -> Result.success(requireNotNull(response.body()))
            response.code() == 400 -> {
                val errorBody = response.errorBody()?.string()
                val summary = HabitTaskApiErrorParser.parse400(errorBody).summary
                Result.failure(Exception("Validación fallida: $summary"))
            }
            response.code() == 500 -> Result.failure(Exception("Error del servidor: ${response.body()?.message}"))
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }

    override suspend fun updateHabit(request: UpdateHabitRequestDto): Result<CreateHabitResponseDto> = try {
        val response = api.updateHabit(request.id, request)
        when {
            response.isSuccessful -> Result.success(requireNotNull(response.body()))
            response.code() == 400 -> {
                val errorBody = response.errorBody()?.string()
                val summary = HabitTaskApiErrorParser.parse400(errorBody).summary
                Result.failure(Exception("Validación fallida: $summary"))
            }
            response.code() == 401 -> Result.failure(Exception("Sesión expirada. Inicia sesión de nuevo."))
            response.code() == 404 -> Result.failure(Exception("Hábito no encontrado"))
            response.code() == 500 -> Result.failure(Exception("Error del servidor: ${response.body()?.message}"))
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }

    override suspend fun deleteHabit(habitId: Int): Result<Unit> = try {
        val response = api.deleteHabit(habitId)
        when {
            response.isSuccessful -> Result.success(Unit)
            response.code() == 401 -> Result.failure(Exception("Sesión expirada. Inicia sesión de nuevo."))
            response.code() == 404 -> Result.failure(Exception("Hábito no encontrado"))
            response.code() == 500 -> Result.failure(Exception("Error del servidor: ${response.body()?.message}"))
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }

    override suspend fun getHabitById(id: Int): Result<HabitDto> = try {
        val response = api.getHabitById(id)
        when {
            response.isSuccessful -> Result.success(requireNotNull(response.body()))
            response.code() == 401 -> Result.failure(Exception("Sesión expirada. Inicia sesión de nuevo."))
            response.code() == 404 -> Result.failure(Exception("Hábito no encontrado"))
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }
}
