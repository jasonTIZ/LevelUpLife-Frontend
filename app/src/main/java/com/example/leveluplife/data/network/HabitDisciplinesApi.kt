package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.HabitDisciplineDto
import retrofit2.Response
import retrofit2.http.GET

interface HabitDisciplinesApi {
    @GET("api/habit/disciplines")
    suspend fun getAll(): Response<List<HabitDisciplineDto>>
}
