package com.example.leveluplife.data.rewards

import com.example.leveluplife.data.network.NetworkModule
import com.example.leveluplife.data.network.RewardApi
import com.example.leveluplife.data.network.dto.ActivateItemResponseDto
import com.example.leveluplife.data.network.dto.ApiErrorEnvelopeDto
import com.example.leveluplife.data.network.dto.InventoryResponseDto
import com.example.leveluplife.data.network.dto.PlayerInventoryDto
import com.example.leveluplife.data.network.dto.PurchaseResponseDto
import com.example.leveluplife.data.network.dto.RewardItemDto
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

interface RewardRepository {
    suspend fun getRewardItems(): Result<List<RewardItemDto>>
    suspend fun purchaseItem(itemId: Int): Result<PurchaseResponseDto>
    suspend fun getInventory(): Result<InventoryResponseDto>
    suspend fun activateItem(inventoryId: Int): Result<ActivateItemResponseDto>
}

class DefaultRewardRepository(private val api: RewardApi) : RewardRepository {

    private val json = NetworkModule.jsonParser()

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

    override suspend fun purchaseItem(itemId: Int): Result<PurchaseResponseDto> = try {
        val response = api.purchaseItem(itemId)
        when {
            response.isSuccessful -> Result.success(requireNotNull(response.body()))
            response.code() == 400 -> Result.failure(Exception(mapPurchaseError(response)))
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }

    override suspend fun getInventory(): Result<InventoryResponseDto> = try {
        val response = api.getInventoryRaw()
        when {
            response.isSuccessful -> {
                val raw = response.body()?.string().orEmpty()
                Result.success(parseInventoryResponse(raw))
            }
            response.code() == 401 -> Result.failure(Exception("unauthorized"))
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }

    override suspend fun activateItem(inventoryId: Int): Result<ActivateItemResponseDto> = try {
        val response = api.activateItem(inventoryId)
        when {
            response.isSuccessful -> Result.success(requireNotNull(response.body()))
            response.code() == 400 -> Result.failure(Exception(mapActivateError(response)))
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }

    private fun parseInventoryResponse(rawBody: String): InventoryResponseDto {
        if (rawBody.isBlank()) return InventoryResponseDto()
        val element = json.parseToJsonElement(rawBody)
        return when (element) {
            is JsonArray -> InventoryResponseDto(
                items = json.decodeFromJsonElement<List<PlayerInventoryDto>>(element),
            )
            is JsonObject -> {
                val itemsNode = element["items"] ?: element["Items"]
                val effectsNode = element["activeEffects"] ?: element["ActiveEffects"]
                InventoryResponseDto(
                    items = itemsNode?.let { json.decodeFromJsonElement(it) } ?: emptyList(),
                    activeEffects = effectsNode?.let { json.decodeFromJsonElement(it) } ?: emptyList(),
                )
            }
            else -> InventoryResponseDto()
        }
    }

    private fun mapPurchaseError(response: retrofit2.Response<*>): String {
        val envelope = parseErrorEnvelope(response)
        val message = envelope?.message.orEmpty().lowercase()
        val details = envelope?.details.orEmpty().lowercase()
        return when {
            message.contains("insufficient") || details.contains("insufficient") ||
                message.contains("gold") && details.contains("only have") ->
                "insufficient_gold"
            else -> "generic"
        }
    }

    private fun mapActivateError(response: retrofit2.Response<*>): String {
        val envelope = parseErrorEnvelope(response)
        val combined = "${envelope?.message.orEmpty()} ${envelope?.details.orEmpty()}".lowercase()
        return when {
            combined.contains("quantity") || combined.contains("no units") ||
                combined.contains("sin unidades") ->
                "no_quantity"
            combined.contains("already active") || combined.contains("ya activo") ||
                combined.contains("effect already") ->
                "effect_already_active"
            combined.contains("recovery") || combined.contains("streak") ||
                combined.contains("recuper") || combined.contains("objetivo") ||
                combined.contains("no target") ->
                "recovery_no_target"
            else -> "generic"
        }
    }

    private fun parseErrorEnvelope(response: retrofit2.Response<*>): ApiErrorEnvelopeDto? {
        val errorBody = response.errorBody()?.string().orEmpty()
        if (errorBody.isBlank()) return null
        return runCatching {
            json.decodeFromString<ApiErrorEnvelopeDto>(errorBody)
        }.getOrNull()
    }
}
