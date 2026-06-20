package com.example.leveluplife.ui.habittaskdetail

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.leveluplife.data.network.dto.HabitTaskDto
import com.example.leveluplife.ui.components.LulErrorAlertDialog
import com.example.leveluplife.ui.components.LulPrimaryButton
import com.example.leveluplife.ui.components.TimerPreview
import com.example.leveluplife.ui.components.showLulSnackbar
import com.example.leveluplife.ui.createtask.CreateHabitTaskOptions

object HabitTaskDetailTestTags {
    const val DEACTIVATE_BUTTON = "task_detail_deactivate_button"
    const val CONFIRM_DIALOG = "task_detail_deactivate_confirm_dialog"
    const val CONFIRM_DEACTIVATE_BUTTON = "task_detail_confirm_deactivate_button"
    const val CANCEL_DEACTIVATE_BUTTON = "task_detail_cancel_deactivate_button"
}

private val MutedGreen = Color(0xFF4ADE80)
private val OrangeWarn = Color(0xFFF59E0B)
private val DangerRed = Color(0xFFEF4444)

@Composable
fun HabitTaskDetailScreen(
    viewModel: HabitTaskDetailViewModel,
    @StringRes successMessageRes: Int? = null,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onViewEvidences: () -> Unit,
    onEdit: ((HabitTaskDto) -> Unit)? = null,
    onTaskDeactivated: (message: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val confirmationMessage = successMessageRes?.let { stringResource(it) }
    val defaultDeactivationMessage = stringResource(R.string.deactivate_task_success)

    LaunchedEffect(successMessageRes) {
        if (confirmationMessage != null) {
            snackbarHostState.showLulSnackbar(confirmationMessage)
        }
    }

    LaunchedEffect(state.taskDeactivated, defaultDeactivationMessage) {
        if (state.taskDeactivated) {
            onTaskDeactivated(defaultDeactivationMessage)
            viewModel.consumeDeactivatedEvent()
        }
    }

    state.reward?.let { reward ->
        TaskCompletionRewardDialog(
            reward = reward,
            onDismiss = viewModel::dismissReward,
        )
    }

    if (state.completionError != null) {
        LulErrorAlertDialog(
            title = stringResource(R.string.error_dialog_title),
            message = state.completionError ?: "",
            dismissText = stringResource(R.string.login_dismiss),
            onDismiss = viewModel::dismissCompletionError,
        )
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
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            DetailHeader(
                onBack = onBack,
                onEdit = state.task?.takeIf { it.isActive }?.let { task ->
                    onEdit?.let { edit -> { edit(task) } }
                },
            )

            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp),
                    )
                }

                state.loadError != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            state.loadError ?: "",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = viewModel::loadTask) {
                            Text(
                                stringResource(R.string.create_task_retry),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                state.task != null -> HabitTaskDetailContent(
                    task = state.task!!,
                    habitTitle = state.habitTitle,
                    isCompleting = state.isCompleting,
                    onDone = onDone,
                    onComplete = viewModel::completeTask,
                    onViewEvidences = onViewEvidences,
                    onRequestDeactivate = viewModel::onRequestDeactivate,
                )
            }
        }
    }
}

@Composable
private fun DetailHeader(
    onBack: () -> Unit,
    onEdit: (() -> Unit)?,
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
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            text = stringResource(R.string.task_detail_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        if (onEdit != null) {
            TextButton(onClick = onEdit) {
                Text(
                    text = stringResource(R.string.update_task_edit),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun HabitTaskDetailContent(
    task: HabitTaskDto,
    habitTitle: String?,
    isCompleting: Boolean,
    onDone: () -> Unit,
    onComplete: () -> Unit,
    onViewEvidences: () -> Unit,
    onRequestDeactivate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!habitTitle.isNullOrBlank()) {
                    Text(
                        text = habitTitle,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = task.title.ifBlank { stringResource(R.string.task_detail_untitled) },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = task.description?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.create_task_preview_no_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (task.isCompleted) {
                        StatusChip(
                            label = stringResource(R.string.task_detail_status_completed),
                            background = MutedGreen.copy(alpha = 0.15f),
                            textColor = MutedGreen,
                        )
                    }
                    StatusChip(
                        label = if (task.isActive) {
                            stringResource(R.string.task_detail_status_active)
                        } else {
                            stringResource(R.string.task_detail_status_inactive)
                        },
                        background = if (task.isActive) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                        },
                        textColor = if (task.isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }

        SectionTitle(stringResource(R.string.task_detail_planning_section))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRow(
                    label = stringResource(R.string.create_task_field_difficulty),
                    value = HabitTaskLabels.difficulty(task.difficulty),
                )
                DetailRow(
                    label = stringResource(R.string.create_task_field_frequency),
                    value = HabitTaskLabels.frequency(task.frequency),
                )
                DetailRow(
                    label = stringResource(R.string.task_detail_plan_duration),
                    value = HabitTaskLabels.periodSummary(task.periodLength, task.periodUnit),
                )
                DetailRow(
                    label = stringResource(R.string.create_task_field_start_date),
                    value = CreateHabitTaskOptions.formatStartDate(task.startDate),
                )
                if (task.xpValue > 0) {
                    DetailRow(
                        label = stringResource(R.string.task_detail_xp),
                        value = stringResource(R.string.task_detail_xp_value, task.xpValue),
                    )
                }
                if (!task.weekDays.isNullOrBlank()) {
                    DetailRow(
                        label = stringResource(R.string.task_detail_week_days),
                        value = HabitTaskLabels.weekDays(task.weekDays),
                    )
                }
            }
        }

        SectionTitle(stringResource(R.string.task_detail_criteria_section))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRow(
                    label = stringResource(R.string.create_task_field_criteria_type),
                    value = HabitTaskLabels.completionCriteria(task.completionCriteria),
                )
                DetailRow(
                    label = stringResource(R.string.task_detail_criteria_goal),
                    value = HabitTaskLabels.criteriaGoalSummary(task),
                )
                task.repetitionCriteria?.let { criteria ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (criteria.isPartialAllowed) {
                            CriteriaChip(
                                label = stringResource(R.string.create_task_partial_allowed),
                                background = OrangeWarn.copy(alpha = 0.15f),
                                textColor = OrangeWarn,
                            )
                        }
                        if (!criteria.isActive) {
                            CriteriaChip(
                                label = stringResource(R.string.task_detail_criteria_inactive),
                                background = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        task.timerCriteria?.takeIf { task.completionCriteria == "TIMER" }?.let { timer ->
            SectionTitle(stringResource(R.string.task_detail_timer_section))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailRow(
                        label = stringResource(R.string.task_detail_timer_duration),
                        value = HabitTaskLabels.formatTimerDuration(timer.numSecondsDefined),
                    )
                    timer.numSecondsLong?.takeIf { it > 0 }?.let { threshold ->
                        DetailRow(
                            label = stringResource(R.string.task_detail_timer_threshold),
                            value = HabitTaskLabels.formatTimerDuration(threshold),
                        )
                    }
                    DetailRow(
                        label = stringResource(R.string.task_detail_timer_pause),
                        value = if (timer.typePauseIsAllowed) {
                            stringResource(R.string.task_detail_timer_pause_yes)
                        } else {
                            stringResource(R.string.task_detail_timer_pause_no)
                        },
                    )
                    Spacer(Modifier.height(4.dp))
                    TimerPreview(
                        durationSeconds = timer.numSecondsDefined,
                        pauseAllowed = timer.typePauseIsAllowed,
                        thresholdSeconds = timer.numSecondsLong,
                    )
                }
            }
        }

        TextButton(
            onClick = onViewEvidences,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                stringResource(R.string.evidence_gallery_view_button),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        if (task.isActive && !task.isCompleted) {
            LulPrimaryButton(
                text = stringResource(R.string.complete_task_button),
                onClick = onComplete,
                enabled = !isCompleting,
                isLoading = isCompleting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MutedGreen,
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledContentColor = MutedGreen.copy(alpha = 0.5f),
                ),
            )
        }

        if (task.isActive) {
            LulPrimaryButton(
                text = stringResource(R.string.deactivate_task_button),
                onClick = onRequestDeactivate,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(HabitTaskDetailTestTags.DEACTIVATE_BUTTON),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                ),
                trailingIcon = Icons.Outlined.WarningAmber,
            )
        }

        Spacer(Modifier.height(8.dp))


        TextButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                stringResource(R.string.task_detail_back_home),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(Modifier.height(16.dp))
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
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                    Text(
                        text = stringResource(R.string.deactivate_task_confirm_ack),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground,
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
                    color = if (state.consequencesAcknowledged) {
                        DangerRed
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        },
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        letterSpacing = 2.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
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
