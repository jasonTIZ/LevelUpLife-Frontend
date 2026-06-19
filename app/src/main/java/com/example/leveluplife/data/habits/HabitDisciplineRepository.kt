package com.example.leveluplife.data.habits

import com.example.leveluplife.data.network.HabitDisciplinesApi
import com.example.leveluplife.data.network.dto.HabitDisciplineDto

interface HabitDisciplineRepository {
    suspend fun getAll(): Result<List<HabitDisciplineDto>>
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
}
