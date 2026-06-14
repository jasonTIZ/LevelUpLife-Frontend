package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.GetPlayerProfileResponseDto
import com.example.leveluplife.data.network.dto.UpdatePlayerProfileRequestDto
import com.example.leveluplife.data.network.dto.UpdatePlayerProfileResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT

interface PlayerApi {
    @GET("api/Player/profile")
    suspend fun getProfile(): Response<GetPlayerProfileResponseDto>

    @PUT("api/Player/update")
    suspend fun updateProfile(
        @Header("If-Match") ifMatch: String,
        @Body body: UpdatePlayerProfileRequestDto,
    ): Response<UpdatePlayerProfileResponseDto>
}
