package com.example.leveluplife.data.rewards

import com.example.leveluplife.data.network.RewardApi
import com.example.leveluplife.data.network.dto.PlayerInventoryDto
import com.example.leveluplife.data.network.dto.RewardItemDto

interface RewardRepository {
    suspend fun getRewardItems(): Result<List<RewardItemDto>>
    suspend fun purchaseItem(itemId: Int): Result<PlayerInventoryDto>
    suspend fun getInventory(): Result<List<PlayerInventoryDto>>
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

    override suspend fun purchaseItem(itemId: Int): Result<PlayerInventoryDto> = try {
        val response = api.purchaseItem(itemId)
        when {
            response.isSuccessful -> Result.success(requireNotNull(response.body()))
            response.code() == 402 -> Result.failure(Exception("insufficient_funds"))
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }

    override suspend fun getInventory(): Result<List<PlayerInventoryDto>> = try {
        val response = api.getInventory()
        when {
            response.isSuccessful -> Result.success(response.body() ?: emptyList())
            response.code() == 401 -> Result.failure(Exception("unauthorized"))
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }
}
