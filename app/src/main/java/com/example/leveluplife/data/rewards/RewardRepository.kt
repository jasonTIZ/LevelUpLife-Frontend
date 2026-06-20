package com.example.leveluplife.data.rewards

import com.example.leveluplife.data.network.RewardApi
import com.example.leveluplife.data.network.dto.RewardItemDto
import com.example.leveluplife.data.network.dto.RewardItemFilterRequest

interface RewardRepository {
    suspend fun getRewardItems(): Result<List<RewardItemDto>>
    suspend fun purchaseRewardItem(filter: RewardItemFilterRequest): Result<List<RewardItemDto>>
}

class DefaultRewardRepository(private val api: RewardApi) : RewardRepository {

    override suspend fun getRewardItems(): Result<List<RewardItemDto>> = try {
        val response = api.listRewardItems()
        when {
            response.isSuccessful -> Result.success(response.body() ?: emptyList())
            response.code() == 401 -> Result.failure(Exception("unauthorized"))
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }

    override suspend fun purchaseRewardItem(filter: RewardItemFilterRequest): Result<List<RewardItemDto>> = try {
        val response = api.purchaseRewardItem(filter)
        when {
            response.isSuccessful -> Result.success(response.body() ?: emptyList())
            response.code() == 401 -> Result.failure(Exception("unauthorized"))
            response.code() == 402 -> Result.failure(Exception("insufficient_funds"))
            response.code() == 404 -> Result.failure(Exception("not_found"))
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }
}
