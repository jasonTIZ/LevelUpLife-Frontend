package com.example.leveluplife.data.network

import com.example.leveluplife.data.network.dto.ActivateItemResponseDto
import com.example.leveluplife.data.network.dto.PurchaseResponseDto
import com.example.leveluplife.data.network.dto.RewardItemDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RewardApi {
    @GET("api/RewardItem")
    suspend fun listRewardItems(): Response<List<RewardItemDto>>

    @POST("api/RewardItem/{itemId}/purchase")
    suspend fun purchaseItem(@Path("itemId") itemId: Int): Response<PurchaseResponseDto>

    @GET("api/RewardItem/inventory")
    suspend fun getInventoryRaw(): Response<ResponseBody>

    @POST("api/RewardItem/inventory/{inventoryId}/activate")
    suspend fun activateItem(@Path("inventoryId") inventoryId: Int): Response<ActivateItemResponseDto>
}
