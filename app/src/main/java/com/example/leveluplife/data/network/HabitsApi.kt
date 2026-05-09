package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.HabitsPageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HabitsApi {
    @GET("api/habits/active")
    suspend fun getActiveHabits(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int,
    ): Response<HabitsPageResponse>
}
