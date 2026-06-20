package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.HabitDto
import com.example.leveluplife.data.network.dto.HabitsPageResponse
import com.example.leveluplife.data.network.dto.CreateHabitRequestDto
import com.example.leveluplife.data.network.dto.CreateHabitResponseDto
import com.example.leveluplife.data.network.dto.UpdateHabitRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface HabitsApi {
    @GET("api/Habits/active")
    suspend fun getActiveHabits(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int,
    ): Response<HabitsPageResponse>

    @POST("api/habits")
    suspend fun createHabit(@Body body: CreateHabitRequestDto): Response<CreateHabitResponseDto>

    @GET("api/Habits/{id}")
    suspend fun getHabitById(@Path("id") id: Int): Response<HabitDto>

    @PUT("api/habits/{id}")
    suspend fun updateHabit(
        @Path("id") id: Int,
        @Body body: UpdateHabitRequestDto,
    ): Response<CreateHabitResponseDto>
}
