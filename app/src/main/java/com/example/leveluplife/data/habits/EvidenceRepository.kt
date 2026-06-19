package com.example.leveluplife.data.habits

import com.example.leveluplife.data.network.HabitTasksApi
import com.example.leveluplife.data.network.dto.EvidenceDto

interface EvidenceRepository {
    suspend fun getTaskEvidences(taskId: Int): Result<List<EvidenceDto>>
}

class DefaultEvidenceRepository(private val api: HabitTasksApi) : EvidenceRepository {

    override suspend fun getTaskEvidences(taskId: Int): Result<List<EvidenceDto>> = try {
        val response = api.getTaskEvidences(taskId)
        when {
            response.isSuccessful -> Result.success(response.body() ?: emptyList())
            response.code() == 404 -> Result.failure(Exception("task_not_found"))
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }
}
