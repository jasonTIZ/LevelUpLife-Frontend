package com.example.leveluplife.ui.pomodoro

import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.network.dto.CreateHabitRequestDto
import com.example.leveluplife.data.network.dto.CreateHabitResponseDto
import com.example.leveluplife.data.network.dto.HabitDto
import com.example.leveluplife.data.network.dto.HabitTaskDto
import com.example.leveluplife.data.network.dto.HabitsPageResponse
import com.example.leveluplife.data.network.dto.TimerCriteriaDto
import com.example.leveluplife.data.network.dto.UpdateHabitRequestDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PomodoroViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun timerTask(
        id: Int,
        title: String,
        active: Boolean = true,
        criteria: String = "TIMER",
        hasTimer: Boolean = true,
        timerActive: Boolean = true,
        seconds: Int = 600,
        threshold: Int? = null,
        pause: Boolean = true,
    ) = HabitTaskDto(
        id = id,
        title = title,
        isActive = active,
        completionCriteria = criteria,
        timerCriteria = if (hasTimer) {
            TimerCriteriaDto(
                numSecondsDefined = seconds,
                numSecondsLong = threshold,
                typePauseIsAllowed = pause,
                statusTimerCriteriaIsActive = timerActive,
            )
        } else {
            null
        },
    )

    private fun repoFor(habit: HabitDto) = object : HabitRepository {
        override suspend fun getActiveHabits(page: Int, pageSize: Int): Result<HabitsPageResponse> =
            Result.success(
                HabitsPageResponse(success = true, habits = listOf(HabitDto(id = habit.id, title = habit.title))),
            )

        override suspend fun createHabit(request: CreateHabitRequestDto): Result<CreateHabitResponseDto> =
            Result.failure(UnsupportedOperationException())

        override suspend fun updateHabit(request: UpdateHabitRequestDto): Result<CreateHabitResponseDto> =
            Result.failure(UnsupportedOperationException())

        override suspend fun getHabitById(id: Int): Result<HabitDto> = Result.success(habit)

        override fun setCurrentUserId(userId: Int) = Unit

        override fun getCurrentUserId(): Int = 1
    }

    @Test
    fun `selectHabit keeps only active timer tasks with an active timer criteria`() = runTest(dispatcher) {
        val habit = HabitDto(
            id = 1,
            title = "Habit",
            tasks = listOf(
                timerTask(1, "ok", seconds = 900, threshold = 1200, pause = false),
                timerTask(2, "inactiveTask", active = false),
                timerTask(3, "reps", criteria = "REPETITIONS"),
                timerTask(4, "noTimer", hasTimer = false),
                timerTask(5, "inactiveTimerCriteria", timerActive = false),
            ),
        )
        val vm = PomodoroViewModel(repoFor(habit))

        vm.selectHabit(1)
        advanceUntilIdle()

        val tasks = vm.state.value.timerTasks
        assertEquals(1, tasks.size)
        val task = tasks.first()
        assertEquals("ok", task.title)
        assertEquals(900, task.durationSeconds)
        assertEquals(1200, task.thresholdSeconds)
        assertEquals(false, task.pauseAllowed)
    }

    @Test
    fun `setMode clears active config and selection`() = runTest(dispatcher) {
        val vm = PomodoroViewModel(repoFor(HabitDto(id = 1, title = "Habit")))
        vm.setFreeMinutes(10)
        assertNotNull(vm.state.value.config)

        vm.setMode(PomodoroMode.FROM_TASK)
        advanceUntilIdle()

        assertNull(vm.state.value.config)
        assertNull(vm.state.value.selectedHabitId)
        assertTrue(vm.state.value.timerTasks.isEmpty())
        assertEquals(PomodoroMode.FROM_TASK, vm.state.value.mode)
    }

    @Test
    fun `invalid free minutes do not set config and surface an error`() = runTest(dispatcher) {
        val vm = PomodoroViewModel(repoFor(HabitDto(id = 1, title = "Habit")))

        vm.onFreeMinutesChange("")
        vm.startFreeTimer()
        assertNull(vm.state.value.config)
        assertNotNull(vm.state.value.freeError)

        vm.onFreeMinutesChange("0")
        vm.startFreeTimer()
        assertNull(vm.state.value.config)
        assertNotNull(vm.state.value.freeError)

        vm.onFreeMinutesChange("99999")
        vm.startFreeTimer()
        assertNull(vm.state.value.config)
        assertNotNull(vm.state.value.freeError)
    }

    @Test
    fun `valid free minutes set config and clear error`() = runTest(dispatcher) {
        val vm = PomodoroViewModel(repoFor(HabitDto(id = 1, title = "Habit")))

        vm.onFreeMinutesChange("25")
        vm.startFreeTimer()

        assertEquals(25 * 60, vm.state.value.config?.durationSeconds)
        assertNull(vm.state.value.freeError)
    }

    @Test
    fun `selectTask maps the timer config from the task`() = runTest(dispatcher) {
        val vm = PomodoroViewModel(repoFor(HabitDto(id = 1, title = "Habit")))

        vm.selectTask(
            PomodoroTimerTask(
                id = 9,
                title = "Focus",
                durationSeconds = 300,
                pauseAllowed = false,
                thresholdSeconds = 600,
            ),
        )

        val config = vm.state.value.config
        assertEquals(300, config?.durationSeconds)
        assertEquals(false, config?.pauseAllowed)
        assertEquals(600, config?.thresholdSeconds)
        assertEquals("Focus", config?.sourceLabel)
    }
}
