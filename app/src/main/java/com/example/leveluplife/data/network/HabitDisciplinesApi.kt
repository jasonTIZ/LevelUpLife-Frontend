package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.HabitDisciplineDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface HabitDisciplinesApi {
    @GET("api/habit/disciplines")
    suspend fun getAll(): Response<List<HabitDisciplineDto>>

    @GET("api/habit/disciplines/{id}")
    suspend fun getById(@Path("id") id: Int): Response<HabitDisciplineDto>
}
