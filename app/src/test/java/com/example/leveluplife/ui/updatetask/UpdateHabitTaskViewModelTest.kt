package com.example.leveluplife.ui.updatetask

import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.habits.HabitTaskConflictFailure
import com.example.leveluplife.data.habits.HabitTaskRepository
import com.example.leveluplife.data.network.dto.CreateHabitTaskRequest
import com.example.leveluplife.data.network.dto.HabitDto
import com.example.leveluplife.data.network.dto.HabitTaskDto
import com.example.leveluplife.data.network.dto.HabitsPageResponse
import com.example.leveluplife.data.network.dto.PaginationDto
import com.example.leveluplife.data.network.dto.RepetitionCriteriaDto
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
class UpdateHabitTaskViewModelTest {

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
    fun `loadTask populates form from repository`() = runTest(testDispatcher) {
        val task = sampleTask(title = "Original")
        val vm = UpdateHabitTaskViewModel(42, FakeHabitRepository(), FakeHabitTaskRepository(task = task))

        advanceUntilIdle()

        val state = vm.state.value
        assertFalse(state.isLoading)
        assertNull(state.loadError)
        assertEquals("Original", state.form.title)
        assertEquals(1, state.form.selectedHabitId)
    }

    @Test
    fun `submit with valid form calls update and emits updated task`() = runTest(testDispatcher) {
        val task = sampleTask(title = "Original")
        val updated = task.copy(title = "Actualizada")
        val taskRepo = FakeHabitTaskRepository(task = task, updateResult = Result.success(updated))
        val vm = UpdateHabitTaskViewModel(42, FakeHabitRepository(), taskRepo)

        advanceUntilIdle()

        vm.onTitleChange("Actualizada")
        vm.submit()

        advanceUntilIdle()

        assertEquals(1, taskRepo.updateCalls)
        assertEquals(updated, vm.state.value.updatedTask)
        assertFalse(vm.state.value.isSubmitting)
    }

    @Test
    fun `submit with invalid form shows validation errors and skips repository`() = runTest(testDispatcher) {
        val taskRepo = FakeHabitTaskRepository(task = sampleTask())
        val vm = UpdateHabitTaskViewModel(42, FakeHabitRepository(), taskRepo)

        advanceUntilIdle()

        vm.onTitleChange("")
        vm.submit()

        advanceUntilIdle()

        assertTrue(vm.state.value.form.showValidationErrors)
        assertEquals(0, taskRepo.updateCalls)
    }

    @Test
    fun `submit without changing original start date allows save even if date is in the past`() = runTest(testDispatcher) {
        val task = sampleTask(title = "Original", startDate = "2026-05-20")
        val updated = task.copy(title = "Solo título cambiado")
        val taskRepo = FakeHabitTaskRepository(task = task, updateResult = Result.success(updated))
        val vm = UpdateHabitTaskViewModel(42, FakeHabitRepository(), taskRepo)

        advanceUntilIdle()

        vm.onTitleChange("Solo título cambiado")
        vm.submit()

        advanceUntilIdle()

        assertEquals(1, taskRepo.updateCalls)
        assertEquals(updated, vm.state.value.updatedTask)
        assertNull(vm.state.value.form.fieldErrors.startDate)
    }

    @Test
    fun `submit with past start date shows validation error`() = runTest(testDispatcher) {
        val taskRepo = FakeHabitTaskRepository(task = sampleTask())
        val vm = UpdateHabitTaskViewModel(42, FakeHabitRepository(), taskRepo)

        advanceUntilIdle()

        vm.onStartDateChange("2020-01-01")
        vm.submit()

        advanceUntilIdle()

        assertTrue(vm.state.value.form.showValidationErrors)
        assertEquals(HabitTaskFieldError.PastDate, vm.state.value.form.fieldErrors.startDate)
        assertEquals(0, taskRepo.updateCalls)
    }

    @Test
    fun `409 conflict shows dialog`() = runTest(testDispatcher) {
        val taskRepo = FakeHabitTaskRepository(
            task = sampleTask(),
            updateResult = Result.failure(HabitTaskConflictFailure("conflict")),
        )
        val vm = UpdateHabitTaskViewModel(42, FakeHabitRepository(), taskRepo)

        advanceUntilIdle()

        vm.submit()

        advanceUntilIdle()

        assertTrue(vm.state.value.showConflictDialog)
        assertFalse(vm.state.value.isSubmitting)
        assertNull(vm.state.value.updatedTask)
    }

    private fun sampleTask(title: String = "Tarea", startDate: String = "2026-05-26") = HabitTaskDto(
        id = 42,
        habitId = 1,
        title = title,
        description = "Desc",
        difficulty = "MEDIUM",
        frequency = "WEEKLY",
        periodLength = 1,
        periodUnit = "WEEKS",
        startDate = startDate,
        completionCriteria = "REPETITIONS",
        repetitionCriteria = RepetitionCriteriaDto(
            repetitions = 3,
            measurementUnit = "SERIES",
            isPartialAllowed = true,
        ),
    )

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
        private val task: HabitTaskDto,
        private val updateResult: Result<HabitTaskDto> = Result.success(task),
    ) : HabitTaskRepository {
        var updateCalls = 0

        override suspend fun createHabitTask(request: CreateHabitTaskRequest): Result<HabitTaskDto> =
            Result.failure(UnsupportedOperationException())

        override suspend fun getHabitTask(taskId: Int): Result<HabitTaskDto> =
            if (taskId == task.id) Result.success(task) else Result.failure(Exception("not found"))

        override suspend fun updateHabitTask(
            taskId: Int,
            request: CreateHabitTaskRequest,
        ): Result<HabitTaskDto> {
            updateCalls++
            return updateResult
        }

        override suspend fun deactivateHabitTask(taskId: Int): Result<String> =
            Result.failure(UnsupportedOperationException())
    }
}
