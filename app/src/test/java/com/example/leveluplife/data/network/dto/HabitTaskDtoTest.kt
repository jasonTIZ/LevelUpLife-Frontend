package com.example.leveluplife.data.network.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HabitTaskDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes nested habitDiscipline from backend response`() {
        val payload = """
            {
              "id": 42,
              "habitId": 1,
              "title": "acostarme a las 9:00",
              "habitDiscipline": {
                "idHabitDiscipline": 7,
                "idHabitCategory": 3,
                "dscHabitDisciplineName": "Sueño",
                "dscHabitDisciplineDescription": "",
                "statusHabitDisciplineIsActive": true
              }
            }
        """.trimIndent()

        val task = json.decodeFromString(HabitTaskDto.serializer(), payload)

        assertNull(task.habitDisciplineId)
        assertEquals(7, task.resolvedDisciplineId)
        assertEquals(3, task.resolvedCategoryId)
        assertEquals("Sueño", task.habitDiscipline?.name)
    }
}
