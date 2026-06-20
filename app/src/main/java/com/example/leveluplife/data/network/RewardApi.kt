package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.PlayerInventoryDto
import com.example.leveluplife.data.network.dto.RewardItemDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RewardApi {
    @GET("api/rewarditem")
    suspend fun listRewardItems(): Response<List<RewardItemDto>>

    @POST("api/rewarditem/{itemId}/purchase")
    suspend fun purchaseItem(@Path("itemId") itemId: Int): Response<PlayerInventoryDto>

    @GET("api/rewarditem/inventory")
    suspend fun getInventory(): Response<List<PlayerInventoryDto>>
}
