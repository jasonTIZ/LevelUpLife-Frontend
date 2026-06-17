package com.example.leveluplife.ui.habitdetail

import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.network.dto.HabitDto
import com.example.leveluplife.data.network.dto.HabitTaskDto
import com.example.leveluplife.data.network.dto.HabitsPageResponse
import com.example.leveluplife.data.network.dto.PaginationDto
import com.example.leveluplife.data.network.dto.RepetitionCriteriaDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HabitDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadHabit success exposes habit with tasks`() = runTest(testDispatcher) {
        val habit = HabitDto(
            id = 5,
            title = "Misión diaria",
            tasks = listOf(
                HabitTaskDto(
                    id = 10,
                    title = "Flexiones",
                    repetitionCriteria = RepetitionCriteriaDto(
                        repetitions = 3,
                        measurementUnit = "SERIES",
                    ),
                ),
            ),
        )
        val vm = HabitDetailViewModel(FakeHabitRepository(habitDetail = Result.success(habit)), habitId = 5)
        advanceUntilIdle()

        val state = vm.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(habit, state.habit)
        assertEquals(1, state.habit?.tasks?.size)
    }

    private class FakeHabitRepository(
        private val habitDetail: Result<HabitDto>,
    ) : HabitRepository {
        override suspend fun getActiveHabits(page: Int, pageSize: Int): Result<HabitsPageResponse> =
            Result.success(HabitsPageResponse(success = true, habits = emptyList(), pagination = PaginationDto(1, 10, 1, 0)))

        override suspend fun getHabitById(id: Int): Result<HabitDto> = habitDetail
    }
}
