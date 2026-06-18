package com.example.leveluplife.ui.habittaskdetail

import com.example.leveluplife.data.habits.HabitTaskCompletionFailure
import com.example.leveluplife.data.habits.HabitTaskRepository
import com.example.leveluplife.data.network.dto.CompleteHabitTaskResponse
import com.example.leveluplife.data.network.dto.CreateHabitTaskRequest
import com.example.leveluplife.data.network.dto.HabitTaskDto
import com.example.leveluplife.data.player.PlayerProfile
import com.example.leveluplife.data.player.ProfileCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
import java.time.Instant

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
    fun `completeTask success updates profile level and shows reward`() = runTest(testDispatcher) {
        val profileCache = FakeProfileCache(level = 2)
        val taskRepo = FakeHabitTaskRepository(
            task = sampleTask(),
            completeResult = Result.success(
                CompleteHabitTaskResponse(
                    xpEarned = 25,
                    newLevel = 3,
                    streakUpdated = true,
                ),
            ),
        )
        val vm = HabitTaskDetailViewModel(42, taskRepo, profileCache, initialTask = sampleTask())

        advanceUntilIdle()

        vm.completeTask()
        advanceUntilIdle()

        assertEquals(1, taskRepo.completeCalls)
        assertEquals(3, profileCache.lastLevel)
        assertNotNull(vm.state.value.reward)
        assertEquals(25, vm.state.value.reward?.xpEarned)
        assertTrue(vm.state.value.reward?.leveledUp == true)
        assertTrue(vm.state.value.task?.isCompleted == true)
    }

    @Test
    fun `double tap while completing sends only one request`() = runTest(testDispatcher) {
        val taskRepo = FakeHabitTaskRepository(task = sampleTask())
        val vm = HabitTaskDetailViewModel(42, taskRepo, FakeProfileCache(), initialTask = sampleTask())

        advanceUntilIdle()

        vm.completeTask()
        vm.completeTask()
        advanceUntilIdle()

        assertEquals(1, taskRepo.completeCalls)
    }

    @Test
    fun `completion failure rolls back optimistic task state`() = runTest(testDispatcher) {
        val taskRepo = FakeHabitTaskRepository(
            task = sampleTask(),
            completeResult = Result.failure(HabitTaskCompletionFailure("Already completed")),
        )
        val vm = HabitTaskDetailViewModel(42, taskRepo, FakeProfileCache(), initialTask = sampleTask())

        advanceUntilIdle()

        vm.completeTask()
        advanceUntilIdle()

        assertFalse(vm.state.value.task?.isCompleted ?: true)
        assertNotNull(vm.state.value.completionError)
        assertNull(vm.state.value.reward)
    }

    private fun sampleTask() = HabitTaskDto(
        id = 42,
        habitId = 1,
        title = "Morning routine",
        isActive = true,
        isCompleted = false,
        xpValue = 25,
    )

    private class FakeProfileCache(level: Int = 1) : ProfileCache {
        private val _profile = MutableStateFlow(
            PlayerProfile(
                playerUserId = "1",
                userName = "tester",
                level = level,
                name = "Test",
                lastName = "User",
                email = "test@example.com",
            ),
        )
        override val profile: StateFlow<PlayerProfile?> = _profile
        var lastLevel: Int = level

        override suspend fun loadPersisted() = Unit

        override suspend fun update(profile: PlayerProfile, etag: String?) {
            _profile.value = profile
            lastLevel = profile.level
        }

        override suspend fun updateLocalExtras(avatarUri: String?, bio: String) = Unit

        override suspend fun updateLevel(level: Int) {
            lastLevel = level
            _profile.value = _profile.value?.copy(level = level)
        }

        override fun currentEtag(): String? = null
    }

    private class FakeHabitTaskRepository(
        private val task: HabitTaskDto,
        private val completeResult: Result<CompleteHabitTaskResponse> = Result.success(
            CompleteHabitTaskResponse(xpEarned = 10, newLevel = 2, streakUpdated = false),
        ),
    ) : HabitTaskRepository {
        var completeCalls = 0

        override suspend fun createHabitTask(request: CreateHabitTaskRequest): Result<HabitTaskDto> =
            Result.failure(UnsupportedOperationException())

        override suspend fun getHabitTask(taskId: Int): Result<HabitTaskDto> =
            if (taskId == task.id) Result.success(task) else Result.failure(Exception("not found"))

        override suspend fun completeHabitTask(
            taskId: Int,
            completedAt: Instant,
        ): Result<CompleteHabitTaskResponse> {
            completeCalls++
            return completeResult
        }
    }
}
