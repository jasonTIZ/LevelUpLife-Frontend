package com.example.leveluplife.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitTaskValidatorsTest {

    @Test
    fun `valid REPETITIONS form has no errors`() {
        val errors = HabitTaskValidators.validateForm(validRepetitionsForm())
        assertFalse(errors.hasErrors)
    }

    @Test
    fun `missing repetition criteria blocks submit`() {
        val errors = HabitTaskValidators.validateForm(
            validRepetitionsForm().copy(
                repetitions = "",
                measurementUnit = null,
            ),
        )
        assertNotNull(errors.repetitions)
        assertNotNull(errors.measurementUnit)
        assertTrue(errors.hasErrors)
    }

    @Test
    fun `TIMER completion does not require repetition fields`() {
        val errors = HabitTaskValidators.validateForm(
            validRepetitionsForm().copy(
                completionCriteria = "TIMER",
                repetitions = "",
                measurementUnit = null,
            ),
        )
        assertNull(errors.repetitions)
        assertNull(errors.measurementUnit)
    }

    @Test
    fun `EVIDENCE requires evidence type`() {
        val errors = HabitTaskValidators.validateForm(
            validRepetitionsForm().copy(
                completionCriteria = "EVIDENCE",
                evidence = null,
            ),
        )
        assertEquals(HabitTaskFieldError.EvidenceRequired, errors.evidence)
    }

    @Test
    fun `title too short returns TooShort`() {
        val error = HabitTaskValidators.validateTitle("ab")
        assertEquals(HabitTaskFieldError.TooShort(3), error)
    }

    private fun validRepetitionsForm() = HabitTaskFormInput(
        habitId = 1,
        title = "Rutina de fuerza",
        description = "Descripción",
        difficulty = "MEDIUM",
        frequency = "WEEKLY",
        periodLength = "1",
        periodUnit = "WEEKS",
        startDate = "2026-05-26",
        completionCriteria = "REPETITIONS",
        repetitions = "3",
        measurementUnit = "SERIES",
        evidence = null,
        isPartialAllowed = true,
    )
}
