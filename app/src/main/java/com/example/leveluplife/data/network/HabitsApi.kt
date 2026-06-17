package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.HabitDto
import com.example.leveluplife.data.network.dto.HabitsPageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HabitsApi {
    @GET("api/Habits/active")
    suspend fun getActiveHabits(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int,
    ): Response<HabitsPageResponse>

    /** Detalle del hábito con tasks[] (repetitionCriteria, timerCriteria). Requiere JWT. */
    @GET("api/Habits/{id}")
    suspend fun getHabitById(@Path("id") id: Int): Response<HabitDto>
}
