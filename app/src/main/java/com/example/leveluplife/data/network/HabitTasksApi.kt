package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.CompleteHabitTaskRequest
import com.example.leveluplife.data.network.dto.CompleteHabitTaskResponse
import com.example.leveluplife.data.network.dto.CreateHabitTaskRequest
import com.example.leveluplife.data.network.dto.HabitTaskDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface HabitTasksApi {
    @POST("api/habit-tasks")
    suspend fun createHabitTask(@Body body: CreateHabitTaskRequest): Response<HabitTaskDto>

    @GET("api/habit-tasks/{id}")
    suspend fun getHabitTask(@Path("id") taskId: Int): Response<HabitTaskDto>

    @PATCH("api/habit-tasks/{id}/complete")
    suspend fun completeHabitTask(
        @Path("id") taskId: Int,
        @Body body: CompleteHabitTaskRequest,
    ): Response<CompleteHabitTaskResponse>
}
