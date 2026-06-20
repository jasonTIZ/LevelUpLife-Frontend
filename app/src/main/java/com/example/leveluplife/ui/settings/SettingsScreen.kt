package com.example.leveluplife.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.leveluplife.R
import com.example.leveluplife.ui.components.LulErrorAlertDialog
import com.example.leveluplife.ui.components.LulPrimaryButton

object SettingsTestTags {
    const val DEACTIVATE_BUTTON = "settings_deactivate_button"
    const val CONFIRM_DIALOG = "settings_confirm_dialog"
    const val CANCEL_BUTTON = "settings_cancel_button"
    const val CONFIRM_DEACTIVATE_BUTTON = "settings_confirm_deactivate_button"
}

private val DangerRed = Color(0xFFEF4444)

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.settings_back),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            ConsequencesCard()

            RecoveryInfoCard()

            Spacer(Modifier.height(8.dp))

            LulPrimaryButton(
                text = stringResource(R.string.settings_deactivate_button),
                onClick = viewModel::onRequestDeactivate,
                modifier = Modifier.testTag(SettingsTestTags.DEACTIVATE_BUTTON),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DangerRed,
                    contentColor = Color.White,
                    disabledContainerColor = DangerRed.copy(alpha = 0.5f),
                ),
                trailingIcon = Icons.Outlined.WarningAmber,
            )
        }
    }

    if (state.showConfirmDialog) {
        DeactivateConfirmDialog(
            state = state,
            onDismiss = viewModel::onCancelDeactivate,
            onConfirm = viewModel::confirmDeactivate,
            onAcknowledgedChange = viewModel::onConsequencesAcknowledgedChange,
            onReasonChange = viewModel::onReasonChange,
        )
    }

    if (state.errorMessage != null) {
        LulErrorAlertDialog(
            title = stringResource(R.string.error_dialog_title),
            message = state.errorMessage ?: "",
            dismissText = stringResource(R.string.login_dismiss),
            onDismiss = viewModel::dismissError,
            errorTestTag = SettingsTestTags.CONFIRM_DIALOG,
        )
    }
}

@Composable
private fun ConsequencesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_deactivate_section_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.settings_deactivate_consequences),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecoveryInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.settings_reactivation_info),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeactivateConfirmDialog(
    state: SettingsUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onAcknowledgedChange: (Boolean) -> Unit,
    onReasonChange: (String) -> Unit,
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
        focusedTextColor = MaterialTheme.colorScheme.onBackground,
        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
        cursorColor = MaterialTheme.colorScheme.primary,
    )

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(SettingsTestTags.CONFIRM_DIALOG),
        icon = {
            Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = DangerRed)
        },
        title = {
            Text(stringResource(R.string.settings_confirm_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.settings_confirm_message))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Checkbox(
                        checked = state.consequencesAcknowledged,
                        onCheckedChange = onAcknowledgedChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                    Text(
                        text = stringResource(R.string.settings_confirm_acknowledge),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f),
                    )
                }

                OutlinedTextField(
                    value = state.reason,
                    onValueChange = onReasonChange,
                    label = { Text(stringResource(R.string.settings_deactivate_reason_label)) },
                    placeholder = { Text(stringResource(R.string.settings_deactivate_reason_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    colors = fieldColors,
                    enabled = !state.isDeactivating,
                )
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss,
                enabled = !state.isDeactivating,
                modifier = Modifier.testTag(SettingsTestTags.CANCEL_BUTTON),
            ) {
                Text(stringResource(R.string.settings_cancel))
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = onConfirm,
                enabled = state.consequencesAcknowledged && !state.isDeactivating,
                modifier = Modifier.testTag(SettingsTestTags.CONFIRM_DEACTIVATE_BUTTON),
            ) {
                Text(
                    text = if (state.isDeactivating) {
                        stringResource(R.string.settings_deactivating)
                    } else {
                        stringResource(R.string.settings_confirm_deactivate)
                    },
                    color = DangerRed,
                )
            }
        },
    )
}
