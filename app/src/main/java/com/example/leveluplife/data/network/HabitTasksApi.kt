package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.CompleteHabitTaskRequest
import com.example.leveluplife.data.network.dto.CompleteHabitTaskResponse
import com.example.leveluplife.data.network.dto.CreateEvidenceRequest
import com.example.leveluplife.data.network.dto.CreateHabitTaskRequest
import com.example.leveluplife.data.network.dto.DeactivateHabitTaskResponse
import com.example.leveluplife.data.network.dto.DeleteEvidenceResponse
import com.example.leveluplife.data.network.dto.EvidenceDto
import com.example.leveluplife.data.network.dto.EvidenceFileUploadResponse
import com.example.leveluplife.data.network.dto.HabitTaskDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface HabitTasksApi {
    @POST("api/habit-tasks")
    suspend fun createHabitTask(@Body body: CreateHabitTaskRequest): Response<HabitTaskDto>

    @GET("api/habit-tasks/{taskId}/evidences")
    suspend fun getTaskEvidences(@Path("taskId") taskId: Int): Response<List<EvidenceDto>>

    @POST("api/habit-tasks/{taskId}/evidences")
    suspend fun createEvidence(
        @Path("taskId") taskId: Int,
        @Body body: CreateEvidenceRequest,
    ): Response<EvidenceDto>

    @Multipart
    @POST("api/evidences/upload")
    suspend fun uploadEvidenceFile(
        @Part file: MultipartBody.Part,
    ): Response<EvidenceFileUploadResponse>

    @DELETE("api/habit-tasks/{taskId}/evidences/{id}")
    suspend fun deleteEvidence(
        @Path("taskId") taskId: Int,
        @Path("id") evidenceId: Int,
    ): Response<DeleteEvidenceResponse>

    @GET("api/habit-tasks/{taskId}")
    suspend fun getHabitTask(@Path("taskId") taskId: Int): Response<HabitTaskDto>

    @PATCH("api/habit-tasks/{id}/complete")
    suspend fun completeHabitTask(
        @Path("id") taskId: Int,
        @Body body: CompleteHabitTaskRequest,
    ): Response<CompleteHabitTaskResponse>

    @PUT("api/habit-tasks/{taskId}")
    suspend fun updateHabitTask(
        @Path("taskId") taskId: Int,
        @Body body: CreateHabitTaskRequest,
    ): Response<HabitTaskDto>

    @DELETE("api/habit-tasks/{id}")
    suspend fun deactivateHabitTask(@Path("id") taskId: Int): Response<DeactivateHabitTaskResponse>
}
