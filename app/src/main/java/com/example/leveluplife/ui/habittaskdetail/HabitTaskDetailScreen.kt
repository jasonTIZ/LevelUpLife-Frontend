package com.example.leveluplife.ui.habittaskdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leveluplife.R
import com.example.leveluplife.ui.components.LulErrorAlertDialog
import com.example.leveluplife.ui.components.LulPrimaryButton
import com.example.leveluplife.ui.theme.DarkBackground
import com.example.leveluplife.ui.theme.DarkOnBackground
import com.example.leveluplife.ui.theme.DarkOnSurfaceVariant
import com.example.leveluplife.ui.theme.DarkSurfaceVariant
import com.example.leveluplife.ui.theme.PurplePrimary
import com.example.leveluplife.ui.theme.PurplePrimaryContainer

object HabitTaskDetailTestTags {
    const val DEACTIVATE_BUTTON = "task_detail_deactivate_button"
    const val CONFIRM_DIALOG = "task_detail_deactivate_confirm_dialog"
    const val CONFIRM_DEACTIVATE_BUTTON = "task_detail_confirm_deactivate_button"
    const val CANCEL_DEACTIVATE_BUTTON = "task_detail_cancel_deactivate_button"
}

private val OrangeWarn = Color(0xFFF59E0B)
private val DangerRed = Color(0xFFEF4444)

@Composable
fun HabitTaskDetailScreen(
    viewModel: HabitTaskDetailViewModel,
    showConfirmation: Boolean,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onTaskDeactivated: (message: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val confirmationMessage = stringResource(R.string.create_task_success_message)
    val defaultDeactivationMessage = stringResource(R.string.deactivate_task_success)

    LaunchedEffect(showConfirmation) {
        if (showConfirmation) {
            snackbarHostState.showSnackbar(confirmationMessage)
        }
    }

    LaunchedEffect(state.taskDeactivated, defaultDeactivationMessage) {
        if (state.taskDeactivated) {
            val message = state.deactivationMessage ?: defaultDeactivationMessage
            onTaskDeactivated(message)
            viewModel.consumeDeactivatedEvent()
        }
    }

    if (state.showConfirmDeactivateDialog) {
        DeactivateTaskConfirmDialog(
            state = state,
            onDismiss = viewModel::onCancelDeactivate,
            onConfirm = viewModel::confirmDeactivate,
            onAcknowledgedChange = viewModel::onConsequencesAcknowledgedChange,
        )
    }

    if (state.deactivateError != null) {
        LulErrorAlertDialog(
            title = stringResource(R.string.error_dialog_title),
            message = state.deactivateError ?: "",
            dismissText = stringResource(R.string.login_dismiss),
            onDismiss = viewModel::dismissDeactivateError,
            errorTestTag = HabitTaskDetailTestTags.CONFIRM_DIALOG,
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = PurplePrimary, strokeWidth = 2.dp)
            }

            state.loadError != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.loadError ?: "",
                        color = DarkOnSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = viewModel::loadTask) {
                        Text(stringResource(R.string.create_task_retry), color = PurplePrimary)
                    }
                }
            }

            state.task != null -> HabitTaskDetailContent(
                task = requireNotNull(state.task),
                contentPadding = innerPadding,
                onBack = onBack,
                onDone = onDone,
                onRequestDeactivate = viewModel::onRequestDeactivate,
            )
        }
    }
}

@Composable
private fun HabitTaskDetailContent(
    task: com.example.leveluplife.data.network.dto.HabitTaskDto,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onRequestDeactivate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.create_task_back),
                    tint = DarkOnBackground,
                )
            }
            Text(
                text = stringResource(R.string.task_detail_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkOnBackground,
                modifier = Modifier.weight(1f),
            )
            if (!task.isActive) {
                StatusChip(
                    label = stringResource(R.string.task_detail_status_inactive),
                    background = DarkOnSurfaceVariant.copy(alpha = 0.15f),
                    textColor = DarkOnSurfaceVariant,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DarkOnBackground,
                    )
                    if (!task.description.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkOnBackground,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(
                            R.string.task_detail_meta,
                            task.difficulty,
                            task.frequency,
                            task.startDate,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkOnSurfaceVariant,
                    )
                }
            }

            Text(
                text = stringResource(R.string.task_detail_criteria_section),
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkOnSurfaceVariant,
            )

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = task.completionCriteria,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkOnBackground,
                    )
                    task.repetitionCriteria?.let { criteria ->
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CriteriaChip(
                                label = criteria.toReadableSummary(),
                                background = PurplePrimaryContainer,
                                textColor = PurplePrimary,
                            )
                            if (criteria.isPartialAllowed) {
                                CriteriaChip(
                                    label = stringResource(R.string.create_task_partial_allowed),
                                    background = OrangeWarn.copy(alpha = 0.15f),
                                    textColor = OrangeWarn,
                                )
                            }
                        }
                    }
                    if (task.evidence != null) {
                        Spacer(Modifier.height(8.dp))
                        CriteriaChip(
                            label = task.evidence,
                            background = PurplePrimaryContainer,
                            textColor = PurplePrimary,
                        )
                    }
                }
            }

            if (task.isActive) {
                LulPrimaryButton(
                    text = stringResource(R.string.deactivate_task_button),
                    onClick = onRequestDeactivate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(HabitTaskDetailTestTags.DEACTIVATE_BUTTON),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DangerRed,
                        contentColor = Color.White,
                        disabledContainerColor = DangerRed.copy(alpha = 0.5f),
                    ),
                    trailingIcon = Icons.Outlined.WarningAmber,
                )
            } else {
                Text(
                    text = stringResource(R.string.deactivate_task_reactivation_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkOnSurfaceVariant,
                )
            }

            Spacer(Modifier.weight(1f))

            TextButton(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.task_detail_back_home), color = PurplePrimary)
            }
        }
    }
}

@Composable
private fun DeactivateTaskConfirmDialog(
    state: HabitTaskDetailUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onAcknowledgedChange: (Boolean) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(HabitTaskDetailTestTags.CONFIRM_DIALOG),
        icon = {
            Icon(
                Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = DangerRed,
            )
        },
        title = {
            Text(
                text = stringResource(R.string.deactivate_task_confirm_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.deactivate_task_confirm_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Checkbox(
                        checked = state.consequencesAcknowledged,
                        onCheckedChange = onAcknowledgedChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = PurplePrimary,
                            uncheckedColor = DarkOnSurfaceVariant,
                        ),
                    )
                    Text(
                        text = stringResource(R.string.deactivate_task_confirm_ack),
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkOnBackground,
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(HabitTaskDetailTestTags.CANCEL_DEACTIVATE_BUTTON),
                enabled = !state.isDeactivating,
            ) {
                Text(stringResource(R.string.settings_cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag(HabitTaskDetailTestTags.CONFIRM_DEACTIVATE_BUTTON),
                enabled = state.consequencesAcknowledged && !state.isDeactivating,
            ) {
                Text(
                    text = if (state.isDeactivating) {
                        stringResource(R.string.deactivate_task_confirming)
                    } else {
                        stringResource(R.string.deactivate_task_confirm_button)
                    },
                    color = if (state.consequencesAcknowledged) DangerRed else DarkOnSurfaceVariant,
                )
            }
        },
    )
}

@Composable
private fun StatusChip(label: String, background: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}

@Composable
private fun CriteriaChip(label: String, background: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}
