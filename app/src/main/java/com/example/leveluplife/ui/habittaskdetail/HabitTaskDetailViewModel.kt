package com.example.leveluplife.ui.habittaskdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.habits.HabitTaskCompletionFailure
import com.example.leveluplife.data.habits.HabitTaskRepository
import com.example.leveluplife.data.network.dto.HabitTaskDto
import com.example.leveluplife.data.player.ProfileCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.Instant

class HabitTaskDetailViewModel(
    private val taskId: Int,
    private val habitTaskRepository: HabitTaskRepository,
    private val profileCache: ProfileCache,
    initialTask: HabitTaskDto? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(
        HabitTaskDetailUiState(
            task = initialTask?.takeIf { it.id == taskId },
            isLoading = initialTask?.id != taskId,
        ),
    )
    val state: StateFlow<HabitTaskDetailUiState> = _state.asStateFlow()

    init {
        if (initialTask?.id != taskId) {
            loadTask()
        }
    }

    fun loadTask() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, loadError = null) }
            habitTaskRepository.getHabitTask(taskId)
                .onSuccess { task ->
                    _state.update { it.copy(isLoading = false, task = task) }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            loadError = mapNetworkMessage(t),
                        )
                    }
                }
        }
    }

    fun completeTask() {
        val current = _state.value
        val task = current.task ?: return
        if (current.isCompleting || task.isCompleted || !task.isActive) return

        val previousLevel = profileCache.profile.value?.level ?: 1
        val taskSnapshot = task

        _state.update {
            it.copy(
                isCompleting = true,
                completionError = null,
                task = task.copy(isCompleted = true),
            )
        }

        viewModelScope.launch {
            habitTaskRepository.completeHabitTask(taskId, Instant.now())
                .onSuccess { response ->
                    profileCache.updateLevel(response.newLevel)
                    _state.update {
                        it.copy(
                            isCompleting = false,
                            task = it.task?.copy(isCompleted = true),
                            reward = TaskCompletionReward(
                                taskTitle = task.title,
                                xpEarned = response.xpEarned,
                                newLevel = response.newLevel,
                                previousLevel = previousLevel,
                                streakUpdated = response.streakUpdated,
                            ),
                        )
                    }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(
                            isCompleting = false,
                            task = taskSnapshot,
                            completionError = mapCompletionError(t),
                        )
                    }
                }
        }
    }

    fun dismissCompletionError() {
        _state.update { it.copy(completionError = null) }
    }

    fun dismissReward() {
        _state.update { it.copy(reward = null) }
    }

    private fun mapNetworkMessage(t: Throwable): String = when (t) {
        is IOException -> "No connection. Check your network and try again."
        else -> t.message ?: "Unknown error"
    }

    private fun mapCompletionError(t: Throwable): String = when (t) {
        is HabitTaskCompletionFailure -> t.message ?: "Could not complete the task."
        is IOException -> "No connection. Check your network and try again."
        else -> t.message ?: "Could not complete the task."
    }

    class Factory(
        private val taskId: Int,
        private val habitTaskRepository: HabitTaskRepository,
        private val profileCache: ProfileCache,
        private val initialTask: HabitTaskDto? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(HabitTaskDetailViewModel::class.java))
            return HabitTaskDetailViewModel(
                taskId,
                habitTaskRepository,
                profileCache,
                initialTask,
            ) as T
        }
    }
}
