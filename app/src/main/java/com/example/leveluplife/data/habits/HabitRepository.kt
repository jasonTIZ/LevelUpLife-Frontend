package com.example.leveluplife.data.habits

import com.example.leveluplife.data.network.HabitsApi
import com.example.leveluplife.data.network.dto.CreateHabitRequestDto
import com.example.leveluplife.data.network.dto.CreateHabitResponseDto
import com.example.leveluplife.data.network.dto.HabitsPageResponse
import com.example.leveluplife.data.network.dto.PaginationDto
import okhttp3.internal.cache.CacheRequest

interface HabitRepository {
    suspend fun getActiveHabits(page: Int, pageSize: Int = 10): Result<HabitsPageResponse>
    suspend fun createHabit(request: CreateHabitRequestDto): Result<CreateHabitResponseDto>
}

class DefaultHabitRepository(private val api: HabitsApi) : HabitRepository {

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
            response.code() == 400 -> Result.failure(Exception("Validación fallida: ${response.body()?.message}"))
            response.code() == 500 -> Result.failure(Exception("Error del servidor: ${response.body()?.message}"))
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }
}
