package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.DisciplineResponse
import com.example.leveluplife.data.network.dto.HabitsApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface DisciplineApi {

    @GET("api/disciplines/{id}")
    suspend fun getDiscipline(
        @Path("id") id: String,
        @Header("Authorization") token: String,
    ): Response<DisciplineResponse>

    @GET("api/habit/disciplines/{disciplineId}")
    suspend fun getHabits(
        @Path("disciplineId") disciplineId: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header("Authorization") token: String,
    ): Response<HabitsApiResponse>
}
