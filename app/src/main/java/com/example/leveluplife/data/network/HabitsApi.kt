package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.HabitsPageResponse
import com.example.leveluplife.data.network.dto.CreateHabitRequestDto
import com.example.leveluplife.data.network.dto.CreateHabitResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface HabitsApi {
    @GET("api/habits/active")
    suspend fun getActiveHabits(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int,
    ): Response<HabitsPageResponse>

    @POST("api/habits")
    suspend fun createHabit(@Body body: CreateHabitRequestDto):
            Response<CreateHabitResponseDto>
}
