package com.example.leveluplife.data.habits

import com.example.leveluplife.data.network.HabitsApi
import com.example.leveluplife.data.network.dto.HabitDto
import com.example.leveluplife.data.network.dto.HabitsPageResponse
import com.example.leveluplife.data.network.dto.PaginationDto

interface HabitRepository {
    suspend fun getActiveHabits(page: Int, pageSize: Int = 10): Result<HabitsPageResponse>
    suspend fun getHabitById(id: Int): Result<HabitDto>
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

    override suspend fun getHabitById(id: Int): Result<HabitDto> = try {
        val response = api.getHabitById(id)
        when {
            response.isSuccessful -> Result.success(requireNotNull(response.body()))
            response.code() == 404 -> Result.failure(Exception("Hábito no encontrado"))
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }
}
