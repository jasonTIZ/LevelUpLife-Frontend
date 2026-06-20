package com.example.leveluplife.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle

@Composable
fun LulErrorAlertDialog(
    title: String,
    message: String,
    dismissText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    titleTextStyle: TextStyle = MaterialTheme.typography.titleLarge,
    messageTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    iconTint: Color = MaterialTheme.colorScheme.error,
    retryText: String? = null,
    onRetry: (() -> Unit)? = null,
    retryTestTag: String? = null,
    errorTestTag: String? = null,
) {
    val dialogModifier = if (errorTestTag != null) modifier.testTag(errorTestTag) else modifier

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = dialogModifier,
        icon = {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = iconTint,
            )
        },
        title = { Text(text = title, style = titleTextStyle) },
        text = { Text(text = message, style = messageTextStyle) },
        dismissButton = if (onRetry != null && retryText != null) {
            {
                TextButton(
                    onClick = onRetry,
                    modifier = if (retryTestTag != null) Modifier.testTag(retryTestTag) else Modifier,
                ) {
                    Text(text = retryText)
                }
            }
        } else {
            null
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText)
            }
        },
    )
}
