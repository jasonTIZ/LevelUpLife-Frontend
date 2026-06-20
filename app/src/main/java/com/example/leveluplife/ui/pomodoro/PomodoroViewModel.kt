package com.example.leveluplife.ui.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.habits.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PomodoroViewModel(
    private val habitRepository: HabitRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PomodoroUiState())
    val state: StateFlow<PomodoroUiState> = _state.asStateFlow()

    fun setMode(mode: PomodoroMode) {
        _state.update {
            it.copy(
                mode = mode,
                config = null,
                selectedHabitId = null,
                selectedTaskId = null,
                timerTasks = emptyList(),
                tasksError = null,
            )
        }
        if (mode == PomodoroMode.FROM_TASK && _state.value.habits.isEmpty()) {
            loadHabits()
        }
    }

    // --- Free mode ---

    fun onFreeMinutesChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(4)
        _state.update { it.copy(freeMinutesInput = digits) }
    }

    fun setFreeMinutes(minutes: Int) {
        _state.update { it.copy(freeMinutesInput = minutes.toString()) }
        startFreeTimer()
    }

    fun startFreeTimer() {
        val minutes = _state.value.freeMinutesInput.toIntOrNull()?.coerceIn(1, MAX_FREE_MINUTES) ?: return
        _state.update {
            it.copy(
                config = PomodoroTimerConfig(
                    durationSeconds = minutes * 60,
                    pauseAllowed = true,
                    thresholdSeconds = null,
                    sourceLabel = null,
                ),
            )
        }
    }

    // --- From task ---

    fun loadHabits() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingHabits = true, habitsError = null) }
            habitRepository.getActiveHabits(page = 1, pageSize = HABITS_PAGE_SIZE)
                .onSuccess { response ->
                    val options = response.habits.orEmpty().map {
                        PomodoroHabitOption(id = it.id, title = it.title)
                    }
                    _state.update { it.copy(isLoadingHabits = false, habits = options) }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(isLoadingHabits = false, habitsError = t.message ?: "Error desconocido")
                    }
                }
        }
    }

    fun selectHabit(habitId: Int) {
        _state.update {
            it.copy(
                selectedHabitId = habitId,
                selectedTaskId = null,
                config = null,
                isLoadingTasks = true,
                tasksError = null,
                timerTasks = emptyList(),
            )
        }
        viewModelScope.launch {
            habitRepository.getHabitById(habitId)
                .onSuccess { habit ->
                    val timerTasks = habit.tasks
                        .filter { it.completionCriteria == "TIMER" && it.timerCriteria != null }
                        .map { task ->
                            val timer = task.timerCriteria!!
                            PomodoroTimerTask(
                                id = task.id,
                                title = task.title,
                                durationSeconds = timer.numSecondsDefined,
                                pauseAllowed = timer.typePauseIsAllowed,
                                thresholdSeconds = timer.numSecondsLong,
                            )
                        }
                    _state.update { it.copy(isLoadingTasks = false, timerTasks = timerTasks) }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(isLoadingTasks = false, tasksError = t.message ?: "Error desconocido")
                    }
                }
        }
    }

    fun selectTask(task: PomodoroTimerTask) {
        _state.update {
            it.copy(
                selectedTaskId = task.id,
                config = PomodoroTimerConfig(
                    durationSeconds = task.durationSeconds,
                    pauseAllowed = task.pauseAllowed,
                    thresholdSeconds = task.thresholdSeconds,
                    sourceLabel = task.title,
                ),
            )
        }
    }

    fun clearTimer() {
        _state.update { it.copy(config = null, selectedTaskId = null) }
    }

    class Factory(
        private val habitRepository: HabitRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(PomodoroViewModel::class.java))
            return PomodoroViewModel(habitRepository) as T
        }
    }

    private companion object {
        const val MAX_FREE_MINUTES = 1440
        const val HABITS_PAGE_SIZE = 50
    }
}
