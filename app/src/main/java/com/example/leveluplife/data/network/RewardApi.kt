package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.RewardItemDto
import com.example.leveluplife.data.network.dto.RewardItemFilterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RewardApi {
    @GET("api/RewardItem")
    suspend fun listRewardItems(): Response<List<RewardItemDto>>

    @POST("api/RewardItem")
    suspend fun purchaseRewardItem(@Body filter: RewardItemFilterRequest): Response<List<RewardItemDto>>
}
