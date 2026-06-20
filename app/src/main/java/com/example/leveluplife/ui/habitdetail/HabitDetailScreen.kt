package com.example.leveluplife.ui.habitdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leveluplife.R
import com.example.leveluplife.data.network.dto.HabitDto
import com.example.leveluplife.data.network.dto.HabitTaskDto
import com.example.leveluplife.data.network.dto.RepetitionCriteriaDto
import com.example.leveluplife.ui.components.LulErrorAlertDialog
import com.example.leveluplife.ui.components.LulPrimaryButton
import com.example.leveluplife.ui.components.showLulSnackbar
import com.example.leveluplife.ui.createtask.HabitTaskEmbeddedForm
import com.example.leveluplife.ui.habittaskdetail.HabitTaskLabels

private val GreenSuccess = Color(0xFF4CAF50)
private val OrangeWarn = Color(0xFFF59E0B)

@Composable
fun HabitDetailScreen(
    viewModel: HabitDetailViewModel,
    onBack: () -> Unit,
    onTaskClick: (HabitTaskDto) -> Unit,
    infoMessage: String? = null,
    onInfoMessageShown: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val updateSuccessMessage = stringResource(R.string.habit_detail_update_success)

    LaunchedEffect(infoMessage) {
        val message = infoMessage?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        viewModel.refreshHabit()
        snackbarHostState.showLulSnackbar(message)
        onInfoMessageShown()
    }

    LaunchedEffect(state.updateSuccess) {
        if (state.updateSuccess) {
            snackbarHostState.showLulSnackbar(updateSuccessMessage)
            viewModel.onUpdateMessageShown()
        }
    }

    if (state.aiDifficultyFailed) {
        LulErrorAlertDialog(
            title = stringResource(R.string.ai_difficulty_failed_title),
            message = stringResource(R.string.ai_difficulty_failed_message),
            dismissText = stringResource(R.string.login_dismiss),
            onDismiss = viewModel::onAiDifficultyAlertShown,
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
                    text = state.habit?.title ?: stringResource(R.string.habit_detail_default_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )

                if (state.habit != null) {
                    if (state.isEditing) {
                        IconButton(onClick = viewModel::cancelEditing) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.habit_detail_cancel),
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    } else {
                        IconButton(onClick = viewModel::startEditing) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = stringResource(R.string.habit_detail_edit_action),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(44.dp),
                    )
                }

                state.error != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error ?: "",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = viewModel::loadHabit) {
                            Text(
                                stringResource(R.string.create_task_retry),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                state.isEditing && state.habit != null -> HabitEditContent(
                    state = state,
                    viewModel = viewModel,
                )

                state.habit != null -> HabitDetailContent(
                    habit = requireNotNull(state.habit),
                    onTaskClick = onTaskClick,
                )
            }
        }
    }
}

@Composable
private fun HabitDetailContent(
    habit: HabitDto,
    onTaskClick: (HabitTaskDto) -> Unit,
) {
    val activeTasks = habit.tasks.filter { it.isActive }


    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { HabitInfoCard(habit = habit, activeTaskCount = activeTasks.size) }

        if (activeTasks.isNotEmpty()) {
            item {
                SectionTitle(stringResource(R.string.habit_detail_tasks_section))
            }
            itemsIndexed(activeTasks) { index, task ->
                TaskCriteriaCard(
                    taskNumber = index + 1,
                    task = task,
                    onClick = { onTaskClick(task) },
                )
            }
        } else {
            item {
                Text(
                    text = stringResource(R.string.habit_detail_no_active_tasks),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun HabitEditContent(
    state: HabitDetailUiState,
    viewModel: HabitDetailViewModel,
) {
    val habit = requireNotNull(state.habit)
    val activeTasks = habit.tasks.filter { it.isActive }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SectionTitle(stringResource(R.string.habit_detail_edit_section)) }

        item {
            val meta = listOfNotNull(
                habit.categoryName.takeIf { it.isNotBlank() },
                habit.disciplineName.takeIf { it.isNotBlank() },
            ).joinToString(" · ")
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            OutlinedTextField(
                value = state.editTitle,
                onValueChange = viewModel::setEditTitle,
                label = { Text(stringResource(R.string.habit_detail_title_field)) },
                placeholder = { Text(stringResource(R.string.habit_detail_title_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                isError = state.editTitleError != null,
                supportingText = state.editTitleError?.let { error ->
                    { Text(error, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
            )
        }

        item {
            OutlinedTextField(
                value = state.editDescription,
                onValueChange = viewModel::setEditDescription,
                label = { Text(stringResource(R.string.habit_detail_description_field)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
            )
        }

        if (activeTasks.isNotEmpty()) {
            item { SectionTitle(stringResource(R.string.habit_detail_tasks_section)) }
            itemsIndexed(activeTasks) { index, task ->
                TaskCriteriaCard(
                    taskNumber = index + 1,
                    task = task,
                    onClick = {},
                )
            }
        }

        item { SectionTitle(stringResource(R.string.habit_detail_new_tasks_section)) }

        itemsIndexed(state.newTasks) { index, taskForm ->
            HabitTaskEmbeddedForm(
                form = taskForm,
                taskNumber = index + 1,
                canRemove = true,
                onRemove = { viewModel.removeNewTask(index) },
                taskDisciplines = state.disciplines,
                isTaskDisciplinesLoading = state.isDisciplinesLoading,
                onDisciplineSelected = { viewModel.onTaskDisciplineChange(index, it) },
                onTitleChange = { viewModel.onTaskTitleChange(index, it) },
                onDescriptionChange = { viewModel.onTaskDescriptionChange(index, it) },
                onDifficultyChange = { viewModel.onTaskDifficultyChange(index, it) },
                onFrequencyChange = { viewModel.onTaskFrequencyChange(index, it) },
                onPeriodLengthChange = { viewModel.onTaskPeriodLengthChange(index, it) },
                onPeriodUnitChange = { viewModel.onTaskPeriodUnitChange(index, it) },
                onStartDateChange = { viewModel.onTaskStartDateChange(index, it) },
                onCompletionCriteriaChange = { viewModel.onTaskCompletionCriteriaChange(index, it) },
                onRepetitionsChange = { viewModel.onTaskRepetitionsChange(index, it) },
                onMeasurementUnitChange = { viewModel.onTaskMeasurementUnitChange(index, it) },
                onEvidenceChange = { viewModel.onTaskEvidenceChange(index, it) },
                onPartialAllowedChange = { viewModel.onTaskPartialAllowedChange(index, it) },
                onTimerSecondsDefinedChange = { viewModel.onTaskTimerSecondsDefinedChange(index, it) },
                onTimerSecondsLongChange = { viewModel.onTaskTimerSecondsLongChange(index, it) },
                onTimerPauseAllowedChange = { viewModel.onTaskTimerPauseAllowedChange(index, it) },
                onApplyTemplate = { viewModel.applyTaskTemplate(index, it) },
                onDismissError = { viewModel.dismissTaskError(index) },
            )
        }

        item {
            Button(
                onClick = viewModel::addNewTask,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.habit_detail_add_task))
            }
        }

        state.saveError?.let { error ->
            item {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        item {
            LulPrimaryButton(
                text = if (state.isSaving) {
                    stringResource(R.string.habit_detail_saving)
                } else {
                    stringResource(R.string.habit_detail_save)
                },
                onClick = viewModel::saveEdits,
                enabled = !state.isSaving,
                isLoading = state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            TextButton(
                onClick = viewModel::cancelEditing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.habit_detail_cancel))
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Spacer(Modifier.height(4.dp))
    Text(
        text = text,
        fontSize = 11.sp,
        letterSpacing = 2.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun HabitInfoCard(habit: HabitDto, activeTaskCount: Int) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = habit.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            val meta = listOfNotNull(
                habit.categoryName.takeIf { it.isNotBlank() },
                habit.disciplineName.takeIf { it.isNotBlank() },
            ).joinToString(" · ")
            if (meta.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (habit.description.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val withCriteria = habit.tasks
                    .filter { it.isActive }
                    .count { task ->
                        task.repetitionCriteria != null ||
                            task.timerCriteria != null ||
                            !task.evidence.isNullOrBlank()
                    }

                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(
                            R.string.habit_detail_active_tasks_count,
                            activeTaskCount,
                        ),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }

                if (withCriteria > 0) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                GreenSuccess.copy(alpha = 0.15f),
                                RoundedCornerShape(8.dp),
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.habit_detail_with_criteria_count, withCriteria),
                            fontSize = 12.sp,
                            color = GreenSuccess,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskCriteriaCard(
    taskNumber: Int,
    task: HabitTaskDto,
    onClick: () -> Unit,
) {
    val criteria = task.repetitionCriteria
    val label = task.title.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.habit_detail_task_default_title, taskNumber)
    val criteriaType = HabitTaskLabels.completionCriteria(task.completionCriteria)
    val goalSummary = HabitTaskLabels.criteriaGoalSummary(task)
    val frequencyLine = HabitTaskLabels.frequency(task.frequency)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.habit_detail_task_subtitle,
                        criteriaType,
                        goalSummary,
                        frequencyLine,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (criteria != null) {
                    Spacer(Modifier.height(6.dp))
                    CriteriaRow(criteria)
                } else {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.habit_detail_no_repetition_criteria),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (task.isCompleted) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = stringResource(R.string.task_detail_status_completed),
                    tint = GreenSuccess,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun CriteriaRow(criteria: RepetitionCriteriaDto) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CriteriaChip(
            label = criteria.toReadableSummary(),
            background = MaterialTheme.colorScheme.primaryContainer,
            textColor = MaterialTheme.colorScheme.primary,
        )
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

@Composable
private fun CriteriaChip(
    label: String,
    background: Color,
    textColor: Color,
) {
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
        )
    }
}
