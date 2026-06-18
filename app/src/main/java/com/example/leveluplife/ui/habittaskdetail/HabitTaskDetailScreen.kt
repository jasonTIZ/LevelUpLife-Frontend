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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leveluplife.R
import com.example.leveluplife.data.network.dto.HabitTaskDto
import com.example.leveluplife.ui.createtask.CreateHabitTaskOptions
import com.example.leveluplife.ui.theme.DarkBackground
import com.example.leveluplife.ui.theme.DarkOnBackground
import com.example.leveluplife.ui.theme.DarkOnSurfaceVariant
import com.example.leveluplife.ui.theme.DarkSurfaceVariant
import com.example.leveluplife.ui.theme.PurplePrimary
import com.example.leveluplife.ui.theme.PurplePrimaryContainer

private val GreenSuccess = Color(0xFF4CAF50)
private val OrangeWarn = Color(0xFFF59E0B)

@Composable
fun HabitTaskDetailScreen(
    viewModel: HabitTaskDetailViewModel,
    @StringRes successMessageRes: Int? = null,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onEdit: ((HabitTaskDto) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val confirmationMessage = successMessageRes?.let { stringResource(it) }

    LaunchedEffect(successMessageRes) {
        if (confirmationMessage != null) {
            snackbarHostState.showSnackbar(confirmationMessage)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
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
                    CircularProgressIndicator(color = PurplePrimary, modifier = Modifier.size(44.dp))
                }

                state.loadError != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.loadError ?: "", color = DarkOnSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = viewModel::loadTask) {
                            Text(stringResource(R.string.create_task_retry), color = PurplePrimary)
                        }
                    }
                }

                state.task != null -> HabitTaskDetailContent(
                    task = state.task!!,
                    habitTitle = state.habitTitle,
                    onDone = onDone,
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
        if (onEdit != null) {
            TextButton(onClick = onEdit) {
                Text(
                    text = stringResource(R.string.update_task_edit),
                    color = PurplePrimary,
                )
            }
        }
    }
}

@Composable
private fun HabitTaskDetailContent(
    task: HabitTaskDto,
    habitTitle: String?,
    onDone: () -> Unit,
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
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!habitTitle.isNullOrBlank()) {
                    Text(
                        text = habitTitle,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = PurplePrimary,
                    )
                }
                Text(
                    text = task.title.ifBlank { stringResource(R.string.task_detail_untitled) },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkOnBackground,
                )
                Text(
                    text = task.description?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.create_task_preview_no_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkOnSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (task.isCompleted) {
                        StatusChip(
                            label = stringResource(R.string.task_detail_status_completed),
                            background = GreenSuccess.copy(alpha = 0.15f),
                            textColor = GreenSuccess,
                        )
                    }
                    StatusChip(
                        label = if (task.isActive) {
                            stringResource(R.string.task_detail_status_active)
                        } else {
                            stringResource(R.string.task_detail_status_inactive)
                        },
                        background = if (task.isActive) {
                            PurplePrimaryContainer
                        } else {
                            DarkOnSurfaceVariant.copy(alpha = 0.12f)
                        },
                        textColor = if (task.isActive) PurplePrimary else DarkOnSurfaceVariant,
                    )
                }
            }
        }

        SectionTitle(stringResource(R.string.task_detail_planning_section))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
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
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
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
                                background = DarkOnSurfaceVariant.copy(alpha = 0.12f),
                                textColor = DarkOnSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.task_detail_back_home), color = PurplePrimary)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        letterSpacing = 2.sp,
        fontWeight = FontWeight.SemiBold,
        color = DarkOnSurfaceVariant,
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = DarkOnSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = DarkOnBackground,
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
