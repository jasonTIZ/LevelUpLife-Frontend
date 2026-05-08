package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.LoginRequest
import com.example.leveluplife.data.network.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>
}
