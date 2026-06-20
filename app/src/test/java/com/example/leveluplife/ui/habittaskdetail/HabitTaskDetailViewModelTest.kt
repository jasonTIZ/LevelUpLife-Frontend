package com.example.leveluplife.ui.habittaskdetail

import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.habits.HabitTaskCompletionFailure
import com.example.leveluplife.data.habits.HabitTaskRepository
import com.example.leveluplife.data.network.dto.CompleteHabitTaskResponse
import com.example.leveluplife.data.network.dto.CreateHabitRequestDto
import com.example.leveluplife.data.network.dto.CreateHabitResponseDto
import com.example.leveluplife.data.network.dto.CreateHabitTaskRequest
import com.example.leveluplife.data.network.dto.HabitDto
import com.example.leveluplife.data.network.dto.HabitTaskDto
import com.example.leveluplife.data.network.dto.HabitsPageResponse
import com.example.leveluplife.data.network.dto.RepetitionCriteriaDto
import com.example.leveluplife.data.network.dto.UpdateHabitRequestDto
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
    fun `loadTask populates task from repository`() = runTest(testDispatcher) {
        val task = sampleTask()
        val vm = HabitTaskDetailViewModel(
            42,
            FakeHabitTaskRepository(task = task),
            FakeHabitRepository(),
            FakeProfileCache(),
            null,
        )

        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.loadError)
        assertEquals(task, vm.state.value.task)
    }

    @Test
    fun `completeTask success updates profile level and shows reward`() = runTest(testDispatcher) {
        val profileCache = FakeProfileCache(level = 2)
        val taskRepo = FakeHabitTaskRepository(
            task = sampleTask(),
            completeResult = Result.success(
                CompleteHabitTaskResponse(
                    xpEarned = 25,
                    previousLevel = 2,
                    newLevel = 3,
                    experiencePointsInCurrentLevel = 50,
                    experiencePointsRequiredForNextLevel = 100,
                    levelProgressPercent = 0.5,
                    leveledUp = true,
                    streakUpdated = true,
                ),
            ),
        )
        val vm = HabitTaskDetailViewModel(
            42,
            taskRepo,
            FakeHabitRepository(),
            profileCache,
            sampleTask(),
        )

        advanceUntilIdle()

        vm.completeTask()
        advanceUntilIdle()

        assertEquals(1, taskRepo.completeCalls)
        assertEquals(3, profileCache.lastLevel)
        assertEquals(25, vm.state.value.reward?.xpEarned)
        assertEquals(2, vm.state.value.reward?.previousLevel)
        assertEquals(3, vm.state.value.reward?.newLevel)
        assertEquals(0.5f, vm.state.value.reward?.progressFraction)
        assertTrue(vm.state.value.reward?.leveledUp == true)
        assertTrue(vm.state.value.task?.isCompleted == true)
    }

    @Test
    fun `completeTask uses previousLevel from API not stale profile cache`() = runTest(testDispatcher) {
        val profileCache = FakeProfileCache(level = 1)
        val taskRepo = FakeHabitTaskRepository(
            task = sampleTask(),
            completeResult = Result.success(
                CompleteHabitTaskResponse(
                    xpEarned = 25,
                    previousLevel = 3,
                    newLevel = 4,
                    experiencePointsInCurrentLevel = 30,
                    experiencePointsRequiredForNextLevel = 120,
                    levelProgressPercent = 0.25,
                    leveledUp = true,
                    streakUpdated = false,
                ),
            ),
        )
        val vm = HabitTaskDetailViewModel(
            42,
            taskRepo,
            FakeHabitRepository(),
            profileCache,
            sampleTask(),
        )

        advanceUntilIdle()

        vm.completeTask()
        advanceUntilIdle()

        assertEquals(3, vm.state.value.reward?.previousLevel)
        assertEquals(4, vm.state.value.reward?.newLevel)
        assertEquals(0.25f, vm.state.value.reward?.progressFraction)
    }

    @Test
    fun `double tap while completing sends only one request`() = runTest(testDispatcher) {
        val taskRepo = FakeHabitTaskRepository(task = sampleTask())
        val vm = HabitTaskDetailViewModel(
            42,
            taskRepo,
            FakeHabitRepository(),
            FakeProfileCache(),
            sampleTask(),
        )

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
        val vm = HabitTaskDetailViewModel(
            42,
            taskRepo,
            FakeHabitRepository(),
            FakeProfileCache(),
            sampleTask(),
        )

        advanceUntilIdle()

        vm.completeTask()
        advanceUntilIdle()

        assertFalse(vm.state.value.task?.isCompleted ?: true)
        assertNotNull(vm.state.value.completionError)
        assertNull(vm.state.value.reward)
    }

    @Test
    fun `confirmDeactivate without acknowledgement does nothing`() = runTest(testDispatcher) {
        val taskRepo = FakeHabitTaskRepository(task = sampleTask())
        val vm = HabitTaskDetailViewModel(
            42,
            taskRepo,
            FakeHabitRepository(),
            FakeProfileCache(),
            sampleTask(),
        )

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
        val vm = HabitTaskDetailViewModel(
            42,
            taskRepo,
            FakeHabitRepository(),
            FakeProfileCache(),
            sampleTask(),
        )

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
        val vm = HabitTaskDetailViewModel(
            42,
            FakeHabitTaskRepository(task = sampleTask()),
            FakeHabitRepository(),
            FakeProfileCache(),
            sampleTask(),
        )

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
        isCompleted = false,
        xpValue = 25,
        repetitionCriteria = RepetitionCriteriaDto(
            repetitions = 3,
            measurementUnit = "SERIES",
            isPartialAllowed = true,
        ),
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
            _profile.value = _profile.value.copy(level = level)
        }

        override suspend fun updateGameplayProgress(
            level: Int,
            totalExperiencePoints: Int,
            experiencePointsInCurrentLevel: Int,
            experiencePointsRequiredForNextLevel: Int,
            levelProgressPercent: Double,
            daysStreak: Int?,
        ) {
            lastLevel = level
            _profile.value = _profile.value.copy(
                level = level,
                totalExperiencePoints = totalExperiencePoints,
                experiencePointsInCurrentLevel = experiencePointsInCurrentLevel,
                experiencePointsRequiredForNextLevel = experiencePointsRequiredForNextLevel,
                levelProgressPercent = levelProgressPercent,
                daysStreak = daysStreak ?: _profile.value.daysStreak,
            )
        }

        override fun clearMemory() = Unit

        override suspend fun clear() = Unit

        override fun currentEtag(): String? = null
    }

    private class FakeHabitTaskRepository(
        private val task: HabitTaskDto,
        private val completeResult: Result<CompleteHabitTaskResponse> = Result.success(
            CompleteHabitTaskResponse(
                xpEarned = 10,
                previousLevel = 1,
                newLevel = 2,
                experiencePointsInCurrentLevel = 10,
                experiencePointsRequiredForNextLevel = 100,
                levelProgressPercent = 0.1,
                leveledUp = true,
                streakUpdated = false,
            ),
        ),
        private val deactivateResult: Result<String> = Result.success("Task deactivated successfully"),
    ) : HabitTaskRepository {
        var completeCalls = 0
        var deactivateCalls = 0

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

        override suspend fun deactivateHabitTask(taskId: Int): Result<String> {
            deactivateCalls++
            return deactivateResult
        }

        override suspend fun updateHabitTask(
            taskId: Int,
            request: CreateHabitTaskRequest,
        ): Result<HabitTaskDto> = Result.failure(UnsupportedOperationException())
    }

    private class FakeHabitRepository : HabitRepository {
        override suspend fun getActiveHabits(page: Int, pageSize: Int): Result<HabitsPageResponse> =
            Result.failure(UnsupportedOperationException())

        override suspend fun getHabitById(habitId: Int): Result<HabitDto> =
            Result.success(HabitDto(id = habitId, title = "Test habit"))

        override suspend fun createHabit(request: CreateHabitRequestDto): Result<CreateHabitResponseDto> =
            Result.failure(UnsupportedOperationException())

        override suspend fun updateHabit(request: UpdateHabitRequestDto): Result<CreateHabitResponseDto> =
            Result.failure(UnsupportedOperationException())

        override suspend fun deleteHabit(habitId: Int): Result<Unit> =
            Result.failure(UnsupportedOperationException())

        override fun setCurrentUserId(userId: Int) = Unit

        override fun getCurrentUserId(): Int = 1
    }
}
