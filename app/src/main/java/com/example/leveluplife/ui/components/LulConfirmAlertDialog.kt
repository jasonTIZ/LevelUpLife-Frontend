package com.example.leveluplife.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle

@Composable
fun LulConfirmAlertDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    titleTextStyle: TextStyle = MaterialTheme.typography.titleLarge,
    messageTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    confirmEnabled: Boolean = true,
    confirmTestTag: String? = null,
    dismissTestTag: String? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(text = title, style = titleTextStyle) },
        text = { Text(text = message, style = messageTextStyle) },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = if (dismissTestTag != null) Modifier.testTag(dismissTestTag) else Modifier,
            ) {
                Text(text = dismissText)
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = confirmEnabled,
                modifier = if (confirmTestTag != null) Modifier.testTag(confirmTestTag) else Modifier,
            ) {
                Text(text = confirmText)
            }
        },
    )
}
