package com.example.leveluplife.ui.habittaskdetail

import com.example.leveluplife.data.habits.HabitTaskRepository
import com.example.leveluplife.data.network.dto.CreateHabitTaskRequest
import com.example.leveluplife.data.network.dto.HabitTaskDto
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HabitTaskDetailViewModelTest {

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
    fun `loadTask populates task from repository`() = runTest(testDispatcher) {
        val task = sampleTask()
        val vm = HabitTaskDetailViewModel(42, FakeHabitTaskRepository(task = task))

        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.loadError)
        assertEquals(task, vm.state.value.task)
    }

    @Test
    fun `confirmDeactivate without acknowledgement does nothing`() = runTest(testDispatcher) {
        val taskRepo = FakeHabitTaskRepository(task = sampleTask())
        val vm = HabitTaskDetailViewModel(42, taskRepo, initialTask = sampleTask())

        advanceUntilIdle()

        vm.onRequestDeactivate()
        vm.confirmDeactivate()

        advanceUntilIdle()

        assertEquals(0, taskRepo.deactivateCalls)
    }

    @Test
    fun `confirmDeactivate calls repository and emits deactivated event`() = runTest(testDispatcher) {
        val taskRepo = FakeHabitTaskRepository(
            task = sampleTask(),
            deactivateResult = Result.success("Task deactivated successfully"),
        )
        val vm = HabitTaskDetailViewModel(42, taskRepo, initialTask = sampleTask())

        advanceUntilIdle()

        vm.onRequestDeactivate()
        vm.onConsequencesAcknowledgedChange(true)
        vm.confirmDeactivate()

        advanceUntilIdle()

        assertEquals(1, taskRepo.deactivateCalls)
        assertTrue(vm.state.value.taskDeactivated)
        assertEquals("Task deactivated successfully", vm.state.value.deactivationMessage)
        assertFalse(vm.state.value.task?.isActive ?: true)
    }

    @Test
    fun `cancelDeactivate closes dialog`() = runTest(testDispatcher) {
        val vm = HabitTaskDetailViewModel(42, FakeHabitTaskRepository(task = sampleTask()), initialTask = sampleTask())

        advanceUntilIdle()

        vm.onRequestDeactivate()
        vm.onCancelDeactivate()

        assertFalse(vm.state.value.showConfirmDeactivateDialog)
        assertFalse(vm.state.value.consequencesAcknowledged)
    }

    private fun sampleTask() = HabitTaskDto(
        id = 42,
        habitId = 1,
        title = "QA task",
        difficulty = "EASY",
        frequency = "DAILY",
        startDate = "2026-06-17",
        completionCriteria = "REPETITIONS",
        isActive = true,
        repetitionCriteria = RepetitionCriteriaDto(
            repetitions = 3,
            measurementUnit = "SERIES",
            isPartialAllowed = true,
        ),
    )

    private class FakeHabitTaskRepository(
        private val task: HabitTaskDto,
        private val deactivateResult: Result<String> = Result.success("Task deactivated successfully"),
    ) : HabitTaskRepository {
        var deactivateCalls = 0

        override suspend fun createHabitTask(request: CreateHabitTaskRequest): Result<HabitTaskDto> =
            Result.failure(UnsupportedOperationException())

        override suspend fun getHabitTask(taskId: Int): Result<HabitTaskDto> =
            if (taskId == task.id) Result.success(task) else Result.failure(Exception("not found"))

        override suspend fun deactivateHabitTask(taskId: Int): Result<String> {
            deactivateCalls++
            return deactivateResult
        }
    }
}
