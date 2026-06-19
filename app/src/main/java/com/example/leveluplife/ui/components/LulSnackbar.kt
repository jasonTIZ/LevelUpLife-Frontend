package com.example.leveluplife.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object LulSnackbarDefaults {
    const val DurationMillis = 1_000L
}

/**
 * Shows a snackbar with a fixed duration across the app.
 * [SnackbarDuration.Short] cannot be customized; we use Indefinite + manual dismiss.
 */
suspend fun SnackbarHostState.showLulSnackbar(
    message: String,
    durationMillis: Long = LulSnackbarDefaults.DurationMillis,
) {
    coroutineScope {
        launch {
            showSnackbar(
                message = message,
                duration = SnackbarDuration.Indefinite,
            )
        }
        delay(durationMillis)
        currentSnackbarData?.dismiss()
    }
}
