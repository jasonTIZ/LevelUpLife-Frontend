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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leveluplife.R
import com.example.leveluplife.data.network.dto.HabitDto
import com.example.leveluplife.data.network.dto.HabitTaskDto
import com.example.leveluplife.data.network.dto.RepetitionCriteriaDto
import com.example.leveluplife.ui.components.showLulSnackbar
import com.example.leveluplife.ui.habittaskdetail.HabitTaskLabels
import com.example.leveluplife.ui.theme.DarkBackground
import com.example.leveluplife.ui.theme.DarkOnBackground
import com.example.leveluplife.ui.theme.DarkOnSurfaceVariant
import com.example.leveluplife.ui.theme.DarkSurfaceVariant
import com.example.leveluplife.ui.theme.PurplePrimary
import com.example.leveluplife.ui.theme.PurplePrimaryContainer

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

    LaunchedEffect(infoMessage) {
        val message = infoMessage?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        viewModel.refreshHabit()
        snackbarHostState.showLulSnackbar(message)
        onInfoMessageShown()
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
                    text = state.habit?.title ?: stringResource(R.string.habit_detail_default_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkOnBackground,
                    modifier = Modifier.weight(1f),
                )
            }

            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = PurplePrimary,
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
                            color = DarkOnSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = viewModel::loadHabit) {
                            Text(stringResource(R.string.create_task_retry), color = PurplePrimary)
                        }
                    }
                }

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
                    color = DarkOnSurfaceVariant,
                )
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
        color = DarkOnSurfaceVariant,
    )
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun HabitInfoCard(habit: HabitDto, activeTaskCount: Int) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = habit.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkOnBackground,
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
                    color = DarkOnSurfaceVariant,
                )
            }

            if (habit.description.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkOnBackground,
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
                        .background(PurplePrimaryContainer, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(
                            R.string.habit_detail_active_tasks_count,
                            activeTaskCount,
                        ),
                        fontSize = 12.sp,
                        color = PurplePrimary,
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
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
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
                    .background(PurplePrimaryContainer, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    tint = PurplePrimary,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkOnBackground,
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
                    color = DarkOnSurfaceVariant,
                )

                if (criteria != null) {
                    Spacer(Modifier.height(6.dp))
                    CriteriaRow(criteria)
                } else {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.habit_detail_no_repetition_criteria),
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkOnSurfaceVariant,
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
        if (!criteria.isActive) {
            CriteriaChip(
                label = stringResource(R.string.task_detail_criteria_inactive),
                background = DarkOnSurfaceVariant.copy(alpha = 0.12f),
                textColor = DarkOnSurfaceVariant,
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
