package com.example.leveluplife.ui.habittaskdetail

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.leveluplife.ui.theme.DarkBackground
import com.example.leveluplife.ui.theme.DarkOnBackground
import com.example.leveluplife.ui.theme.DarkOnSurfaceVariant
import com.example.leveluplife.ui.theme.DarkSurfaceVariant
import com.example.leveluplife.ui.theme.PurplePrimary
import com.example.leveluplife.ui.theme.PurplePrimaryContainer

object HabitTaskDetailTestTags {
    const val COMPLETE_BUTTON = "task_detail_complete_button"
}

private val GreenSuccess = Color(0xFF4CAF50)
private val OrangeWarn = Color(0xFFF59E0B)

@Composable
fun HabitTaskDetailScreen(
    viewModel: HabitTaskDetailViewModel,
    showConfirmation: Boolean,
    onBack: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val confirmationMessage = stringResource(R.string.create_task_success_message)

    LaunchedEffect(showConfirmation) {
        if (showConfirmation) {
            snackbarHostState.showSnackbar(confirmationMessage)
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
                isCompleting = state.isCompleting,
                contentPadding = innerPadding,
                onBack = onBack,
                onDone = onDone,
                onComplete = viewModel::completeTask,
            )
        }
    }
}

@Composable
private fun HabitTaskDetailContent(
    task: HabitTaskDto,
    isCompleting: Boolean,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onComplete: () -> Unit,
) {
    val canComplete = task.isActive && !task.isCompleted && !isCompleting

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
            if (task.isCompleted) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = stringResource(R.string.task_detail_status_completed),
                    tint = GreenSuccess,
                    modifier = Modifier.size(22.dp),
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
                    if (task.xpValue > 0) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.task_detail_xp_value, task.xpValue),
                            style = MaterialTheme.typography.bodySmall,
                            color = PurplePrimary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
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

            if (canComplete) {
                LulPrimaryButton(
                    text = stringResource(R.string.complete_task_button),
                    onClick = onComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(HabitTaskDetailTestTags.COMPLETE_BUTTON),
                    enabled = canComplete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenSuccess,
                        contentColor = Color.White,
                        disabledContainerColor = GreenSuccess.copy(alpha = 0.5f),
                    ),
                )
            } else if (isCompleting) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = PurplePrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(28.dp),
                    )
                }
            } else if (task.isCompleted) {
                Text(
                    text = stringResource(R.string.complete_task_already_completed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = GreenSuccess,
                    fontWeight = FontWeight.Medium,
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
private fun CriteriaChip(label: String, background: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}
