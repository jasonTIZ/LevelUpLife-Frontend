package com.example.leveluplife.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

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
                timerSecondsDefined = "1800",
            ),
        )
        assertNull(errors.repetitions)
        assertNull(errors.measurementUnit)
        assertNull(errors.timerSeconds)
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

    @Test
    fun `invalid start date returns InvalidDate`() {
        val error = HabitTaskValidators.validateStartDate("2026-13-40")
        assertEquals(HabitTaskFieldError.InvalidDate, error)
    }

    @Test
    fun `incomplete start date returns InvalidDate`() {
        val error = HabitTaskValidators.validateStartDate("2026-05-2")
        assertEquals(HabitTaskFieldError.InvalidDate, error)
    }

    @Test
    fun `past start date returns PastDate`() {
        val error = HabitTaskValidators.validateStartDate(
            value = "2020-01-01",
            options = HabitTaskValidationOptions(today = LocalDate.of(2026, 5, 26)),
        )
        assertEquals(HabitTaskFieldError.PastDate, error)
    }

    @Test
    fun `preserved start date allows past date on edit`() {
        val error = HabitTaskValidators.validateStartDate(
            value = "2020-01-01",
            options = HabitTaskValidationOptions(
                today = LocalDate.of(2026, 5, 26),
                preservedStartDate = "2020-01-01",
            ),
        )
        assertNull(error)
    }

    @Test
    fun `changed start date to past returns PastDate even with preserved original`() {
        val error = HabitTaskValidators.validateStartDate(
            value = "2021-06-15",
            options = HabitTaskValidationOptions(
                today = LocalDate.of(2026, 5, 26),
                preservedStartDate = "2020-01-01",
            ),
        )
        assertEquals(HabitTaskFieldError.PastDate, error)
    }

    @Test
    fun `invalid difficulty returns InvalidOption`() {
        val errors = HabitTaskValidators.validateForm(
            validRepetitionsForm().copy(difficulty = "SUPER_HARD"),
        )
        assertEquals(HabitTaskFieldError.InvalidOption, errors.difficulty)
    }

    @Test
    fun `blank period length is required`() {
        val errors = HabitTaskValidators.validateForm(
            validRepetitionsForm().copy(periodLength = ""),
        )
        assertEquals(HabitTaskFieldError.Required, errors.periodLength)
    }

    @Test
    fun `TIMER requires timer seconds`() {
        val errors = HabitTaskValidators.validateForm(
            validRepetitionsForm().copy(
                completionCriteria = "TIMER",
                timerSecondsDefined = "",
            ),
        )
        assertEquals(HabitTaskFieldError.CriteriaRequired, errors.timerSeconds)
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
        timerSecondsDefined = "",
    )
}
