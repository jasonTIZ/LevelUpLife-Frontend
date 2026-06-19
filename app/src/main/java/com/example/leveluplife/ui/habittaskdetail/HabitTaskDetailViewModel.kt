package com.example.leveluplife.ui.habittaskdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.habits.HabitRepository
import com.example.leveluplife.data.habits.HabitTaskRepository
import com.example.leveluplife.data.habits.HabitTaskValidationFailure
import com.example.leveluplife.data.network.dto.HabitTaskDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class HabitTaskDetailViewModel(
    private val taskId: Int,
    private val habitTaskRepository: HabitTaskRepository,
    private val habitRepository: HabitRepository,
    initialTask: HabitTaskDto?,
) : ViewModel() {

    private val _state = MutableStateFlow(
        HabitTaskDetailUiState(
            task = initialTask?.takeIf { isPreviewUsable(it) },
            isLoading = initialTask == null || !isPreviewUsable(initialTask),
        ),
    )
    val state: StateFlow<HabitTaskDetailUiState> = _state.asStateFlow()

    init {
        loadTask()
    }

    fun loadTask() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, loadError = null) }
            habitTaskRepository.getHabitTask(taskId)
                .onSuccess { task ->
                    val habitTitle = habitRepository.getHabitById(task.habitId)
                        .getOrNull()
                        ?.title
                        ?.takeIf { it.isNotBlank() }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            task = task,
                            habitTitle = habitTitle,
                        )
                    }
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

    fun onRequestDeactivate() {
        _state.update {
            it.copy(
                showConfirmDeactivateDialog = true,
                consequencesAcknowledged = false,
                deactivateError = null,
            )
        }
    }

    fun onCancelDeactivate() {
        _state.update {
            it.copy(
                showConfirmDeactivateDialog = false,
                consequencesAcknowledged = false,
                deactivateError = null,
            )
        }
    }

    fun onConsequencesAcknowledgedChange(value: Boolean) {
        _state.update { it.copy(consequencesAcknowledged = value) }
    }

    fun dismissDeactivateError() {
        _state.update { it.copy(deactivateError = null) }
    }

    fun consumeDeactivatedEvent() {
        _state.update { it.copy(taskDeactivated = false, deactivationMessage = null) }
    }

    fun confirmDeactivate() {
        val current = _state.value
        if (!current.consequencesAcknowledged || current.isDeactivating || current.task == null) return

        viewModelScope.launch {
            _state.update { it.copy(isDeactivating = true, deactivateError = null) }
            habitTaskRepository.deactivateHabitTask(taskId)
                .onSuccess { message ->
                    _state.update {
                        it.copy(
                            isDeactivating = false,
                            showConfirmDeactivateDialog = false,
                            taskDeactivated = true,
                            deactivationMessage = message,
                            task = it.task?.copy(isActive = false),
                        )
                    }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(
                            isDeactivating = false,
                            deactivateError = mapSubmitError(t),
                        )
                    }
                }
        }
    }

    private fun mapNetworkMessage(t: Throwable): String = when (t) {
        is IOException -> "Sin conexión. Revisa tu red e intenta de nuevo."
        else -> t.message ?: "No se pudo cargar la tarea."
    }

    private fun mapSubmitError(t: Throwable): String = when (t) {
        is IOException -> "Sin conexión. Revisa tu red e intenta de nuevo."
        is HabitTaskValidationFailure -> t.message ?: "No se pudo completar la operación."
        else -> t.message ?: "No se pudo desactivar la tarea."
    }

    private fun isPreviewUsable(task: HabitTaskDto): Boolean =
        task.title.isNotBlank() && !task.title.startsWith("Task #")

    class Factory(
        private val taskId: Int,
        private val habitTaskRepository: HabitTaskRepository,
        private val habitRepository: HabitRepository,
        private val initialTask: HabitTaskDto?,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(HabitTaskDetailViewModel::class.java))
            return HabitTaskDetailViewModel(
                taskId,
                habitTaskRepository,
                habitRepository,
                initialTask,
            ) as T
        }
    }
}
