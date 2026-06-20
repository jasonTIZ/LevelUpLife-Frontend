package com.example.leveluplife.ui.pomodoro

enum class PomodoroMode { FREE, FROM_TASK }

/** Habit option for the "from task" picker (no tasks loaded yet). */
data class PomodoroHabitOption(
    val id: Int,
    val title: String,
)

/** A task with TIMER criteria, resolved from a habit's detail. */
data class PomodoroTimerTask(
    val id: Int,
    val title: String,
    val durationSeconds: Int,
    val pauseAllowed: Boolean,
    val thresholdSeconds: Int?,
)

/** Active timer configuration feeding the countdown. */
data class PomodoroTimerConfig(
    val durationSeconds: Int,
    val pauseAllowed: Boolean,
    val thresholdSeconds: Int?,
    val sourceLabel: String?,
)

data class PomodoroUiState(
    val mode: PomodoroMode = PomodoroMode.FREE,
    val freeMinutesInput: String = "25",
    val config: PomodoroTimerConfig? = null,
    // "from task" pickers
    val isLoadingHabits: Boolean = false,
    val habits: List<PomodoroHabitOption> = emptyList(),
    val habitsError: String? = null,
    val selectedHabitId: Int? = null,
    val isLoadingTasks: Boolean = false,
    val timerTasks: List<PomodoroTimerTask> = emptyList(),
    val tasksError: String? = null,
    val selectedTaskId: Int? = null,
)
