package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.CreateHabitTaskRequest
import com.example.leveluplife.data.network.dto.EvidenceDto
import com.example.leveluplife.data.network.dto.HabitTaskDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface HabitTasksApi {
    @POST("api/habit-tasks")
    suspend fun createHabitTask(@Body body: CreateHabitTaskRequest): Response<HabitTaskDto>

    @GET("api/habit-tasks/{taskId}/evidences")
    suspend fun getTaskEvidences(@Path("taskId") taskId: Int): Response<List<EvidenceDto>>
}
