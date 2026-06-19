package com.example.leveluplife.ui.components

import android.view.KeyEvent as AndroidKeyEvent
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.example.leveluplife.domain.validation.ProfileInputSanitizer

/** Filtra espacios en pegado; la tecla espacio se bloquea con [blockPasswordWhitespaceKeys]. */
fun passwordInputChangeHandler(onChange: (String) -> Unit): (String) -> Unit = { raw ->
    onChange(ProfileInputSanitizer.sanitizePassword(raw))
}

/** Consume la tecla espacio para que no se pueda escribir en el campo. */
fun Modifier.blockPasswordWhitespaceKeys(): Modifier = onPreviewKeyEvent { event ->
    event.type == KeyEventType.KeyDown &&
        event.nativeKeyEvent.keyCode == AndroidKeyEvent.KEYCODE_SPACE
}
