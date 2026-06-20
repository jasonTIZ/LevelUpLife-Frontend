package com.example.leveluplife.data.network.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HabitCategoryDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes categories page with data array from backend`() {
        val payload = """
            {
              "success": true,
              "data": [
                {
                  "id": 1,
                  "name": "Salud",
                  "description": "Bienestar físico",
                  "imageUrl": null,
                  "habitsCount": 0,
                  "isActive": true
                },
                {
                  "id": 2,
                  "name": "Productividad",
                  "description": "",
                  "habitsCount": 1,
                  "isActive": true
                }
              ],
              "pagination": {
                "currentPage": 1,
                "pageSize": 10,
                "totalPages": 1,
                "totalRecords": 2
              }
            }
        """.trimIndent()

        val response = json.decodeFromString(HabitCategoriesPageResponse.serializer(), payload)

        assertEquals(2, response.categories.size)
        assertEquals("Salud", response.categories.first().name)
        assertEquals(1, response.resolvedPagination?.currentPage)
        assertEquals(1, response.resolvedPagination?.totalPages)
    }
}
