package com.example.leveluplife.data.habits

import com.example.leveluplife.data.network.HabitDisciplinesApi
import com.example.leveluplife.data.network.dto.HabitDisciplineDto

interface HabitDisciplineRepository {
    suspend fun getAll(): Result<List<HabitDisciplineDto>>
    suspend fun getById(id: Int): Result<HabitDisciplineDto>
}

class DefaultHabitDisciplineRepository(
    private val api: HabitDisciplinesApi,
) : HabitDisciplineRepository {
    override suspend fun getAll(): Result<List<HabitDisciplineDto>> = try {
        val response = api.getAll()
        when {
            response.isSuccessful -> Result.success(response.body() ?: emptyList())
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }

    override suspend fun getById(id: Int): Result<HabitDisciplineDto> = try {
        val response = api.getById(id)
        when {
            response.isSuccessful -> {
                val body = response.body()
                if (body != null) Result.success(body)
                else Result.failure(Exception("HTTP ${response.code()}"))
            }
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }
}
