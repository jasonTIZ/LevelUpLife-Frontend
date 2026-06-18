package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.CreateHabitTaskRequest
import com.example.leveluplife.data.network.dto.DeactivateHabitTaskResponse
import com.example.leveluplife.data.network.dto.HabitTaskDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface HabitTasksApi {
    @POST("api/habit-tasks")
    suspend fun createHabitTask(@Body body: CreateHabitTaskRequest): Response<HabitTaskDto>

    @GET("api/habit-tasks/{id}")
    suspend fun getHabitTask(@Path("id") taskId: Int): Response<HabitTaskDto>

    @DELETE("api/habit-tasks/{id}")
    suspend fun deactivateHabitTask(@Path("id") taskId: Int): Response<DeactivateHabitTaskResponse>
}
