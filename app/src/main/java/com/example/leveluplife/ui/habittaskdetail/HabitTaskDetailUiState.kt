package com.example.leveluplife.ui.habittaskdetail

import com.example.leveluplife.data.network.dto.HabitTaskDto

data class HabitTaskDetailUiState(
    val task: HabitTaskDto? = null,
    val habitTitle: String? = null,
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val showConfirmDeactivateDialog: Boolean = false,
    val consequencesAcknowledged: Boolean = false,
    val isDeactivating: Boolean = false,
    val deactivateError: String? = null,
    val taskDeactivated: Boolean = false,
    val deactivationMessage: String? = null,
)
