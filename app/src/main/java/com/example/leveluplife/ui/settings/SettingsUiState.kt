package com.example.leveluplife.ui.settings

data class SettingsUiState(
    val showConfirmDialog: Boolean = false,
    val consequencesAcknowledged: Boolean = false,
    val reason: String = "",
    val isDeactivating: Boolean = false,
    val errorMessage: String? = null,
)
