package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.LoginRequest
import com.example.leveluplife.data.network.dto.LoginResponse
import com.example.leveluplife.data.network.dto.LogoutResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST(ApiRoutes.AUTH_LOGIN)
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST(ApiRoutes.AUTH_LOGOUT)
    suspend fun logout(): Response<LogoutResponse>
}
