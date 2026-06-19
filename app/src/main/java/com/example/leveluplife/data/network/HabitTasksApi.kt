package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.CreateHabitTaskRequest
import com.example.leveluplife.data.network.dto.DeactivateHabitTaskResponse
import com.example.leveluplife.data.network.dto.DeleteEvidenceResponse
import com.example.leveluplife.data.network.dto.EvidenceDto
import com.example.leveluplife.data.network.dto.HabitTaskDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface HabitTasksApi {
    @POST("api/habit-tasks")
    suspend fun createHabitTask(@Body body: CreateHabitTaskRequest): Response<HabitTaskDto>

    @GET("api/habit-tasks/{taskId}/evidences")
    suspend fun getTaskEvidences(@Path("taskId") taskId: Int): Response<List<EvidenceDto>>

    @DELETE("api/habit-tasks/{taskId}/evidences/{id}")
    suspend fun deleteEvidence(
        @Path("taskId") taskId: Int,
        @Path("id") evidenceId: Int,
    ): Response<DeleteEvidenceResponse>

    @GET("api/habit-tasks/{taskId}")
    suspend fun getHabitTask(@Path("taskId") taskId: Int): Response<HabitTaskDto>

    @PUT("api/habit-tasks/{taskId}")
    suspend fun updateHabitTask(
        @Path("taskId") taskId: Int,
        @Body body: CreateHabitTaskRequest,
    ): Response<HabitTaskDto>

    @DELETE("api/habit-tasks/{id}")
    suspend fun deactivateHabitTask(@Path("id") taskId: Int): Response<DeactivateHabitTaskResponse>
}
