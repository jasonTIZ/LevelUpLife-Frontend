package com.example.leveluplife.ui.createtask

import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.habits.HabitTaskRepository
import com.example.leveluplife.data.network.dto.CreateHabitTaskRequest
import com.example.leveluplife.data.network.dto.HabitDto
import com.example.leveluplife.data.network.dto.HabitTaskDto
import com.example.leveluplife.data.network.dto.HabitsPageResponse
import com.example.leveluplife.data.network.dto.PaginationDto
import com.example.leveluplife.domain.validation.HabitTaskFieldError
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateHabitTaskViewModelTest {

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
    fun `submit without repetition criteria shows validation errors and skips repository`() = runTest(testDispatcher) {
        val habitRepo = FakeHabitRepository()
        val taskRepo = FakeHabitTaskRepository()
        val vm = CreateHabitTaskViewModel(habitRepo, taskRepo, preselectedHabitId = 1)

        advanceUntilIdle()

        vm.onCompletionCriteriaChange("REPETITIONS")
        vm.onRepetitionsChange("")
        vm.onMeasurementUnitChange("")
        vm.onTitleChange("Valid title")
        vm.submit()

        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state.showValidationErrors)
        assertNotNull(state.fieldErrors.repetitions)
        assertEquals(0, taskRepo.createCalls)
    }

    @Test
    fun `submit with valid form calls repository and emits created task`() = runTest(testDispatcher) {
        val habitRepo = FakeHabitRepository()
        val created = HabitTaskDto(
            id = 99,
            habitId = 1,
            title = "Rutina de fuerza",
            completionCriteria = "REPETITIONS",
            repetitionCriteria = null,
        )
        val taskRepo = FakeHabitTaskRepository(result = Result.success(created))
        val vm = CreateHabitTaskViewModel(habitRepo, taskRepo, preselectedHabitId = 1)

        advanceUntilIdle()

        vm.applyTemplate(TaskFormTemplates.all.first())
        vm.onTitleChange("Rutina de fuerza")
        vm.submit()

        advanceUntilIdle()

        assertEquals(1, taskRepo.createCalls)
        assertEquals(created, vm.state.value.createdTask)
        assertFalse(vm.state.value.isSubmitting)
    }

    @Test
    fun `submit with EVIDENCE template calls repository`() = runTest(testDispatcher) {
        val habitRepo = FakeHabitRepository()
        val taskRepo = FakeHabitTaskRepository()
        val vm = CreateHabitTaskViewModel(habitRepo, taskRepo, preselectedHabitId = 1)

        advanceUntilIdle()

        val evidenceTemplate = TaskFormTemplates.all.first { it.completionCriteria == "EVIDENCE" }
        vm.applyTemplate(evidenceTemplate)
        vm.submit()

        advanceUntilIdle()

        assertEquals(1, taskRepo.createCalls)
        assertFalse(vm.state.value.isSubmitting)
    }

    private class FakeHabitRepository : HabitRepository {
        override suspend fun getActiveHabits(page: Int, pageSize: Int): Result<HabitsPageResponse> =
            Result.success(
                HabitsPageResponse(
                    success = true,
                    habits = listOf(
                        HabitDto(
                            id = 1,
                            title = "Rutina de Fuerza Semanal",
                            categoryName = "Salud",
                            disciplineName = "Ejercicio",
                            isActive = true,
                        ),
                    ),
                    pagination = PaginationDto(1, 10, 1, 1),
                ),
            )

        override suspend fun getHabitById(id: Int): Result<HabitDto> =
            Result.failure(UnsupportedOperationException())
    }

    private class FakeHabitTaskRepository(
        private val result: Result<HabitTaskDto> = Result.success(
            HabitTaskDto(id = 1, habitId = 1, title = "Test"),
        ),
    ) : HabitTaskRepository {
        var createCalls = 0

        override suspend fun createHabitTask(request: CreateHabitTaskRequest): Result<HabitTaskDto> {
            createCalls++
            return result
        }
    }
}
