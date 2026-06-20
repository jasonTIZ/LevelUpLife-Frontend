package com.example.leveluplife.data.network.dto

import com.example.leveluplife.data.network.NetworkModule
import org.junit.Assert.assertEquals
import org.junit.Test

class InventoryResponseDtoTest {

    private val json = NetworkModule.jsonParser()

    @Test
    fun `decodes wrapped inventory response from backend`() {
        val response = json.decodeFromString<InventoryResponseDto>(
            """
            {
              "items": [
                {
                  "id": 1,
                  "playerUserId": 2,
                  "rewardItemId": 1,
                  "rewardItemName": "Escudo 1 día",
                  "rewardItemTypeId": 1,
                  "rewardItemTypeName": "Protección de racha",
                  "costGold": 30,
                  "effectValue": 1,
                  "quantity": 1,
                  "isEquipped": false,
                  "acquiredAt": "2026-06-20T02:57:07.143016-06:00"
                }
              ],
              "activeEffects": []
            }
            """.trimIndent(),
        )

        assertEquals(1, response.items.size)
        assertEquals("Escudo 1 día", response.items.first().rewardItemName)
        assertEquals(2, response.items.first().playerUserId)
        assertEquals(0, response.activeEffects.size)
    }
}
