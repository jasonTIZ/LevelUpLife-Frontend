package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.AiChatRequest
import com.example.leveluplife.data.network.dto.AiChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AiApi {
    @POST("api/ai/chat")
    suspend fun chat(@Body body: AiChatRequest): Response<AiChatResponse>
}
