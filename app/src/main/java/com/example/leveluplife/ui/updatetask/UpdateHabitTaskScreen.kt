package com.example.leveluplife.ui.updatetask

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.leveluplife.R
import com.example.leveluplife.data.network.dto.HabitTaskDto
import com.example.leveluplife.ui.components.LulErrorAlertDialog
import com.example.leveluplife.ui.createtask.HabitTaskFormBottomBar
import com.example.leveluplife.ui.createtask.HabitTaskFormContent
import com.example.leveluplife.ui.theme.DarkBackground
import com.example.leveluplife.ui.theme.DarkOnSurfaceVariant
import com.example.leveluplife.ui.theme.PurplePrimary

object UpdateHabitTaskTestTags {
    const val CONFLICT_DIALOG = "update_task_conflict_dialog"
    const val CONFLICT_RELOAD = "update_task_conflict_reload"
}

@Composable
fun UpdateHabitTaskScreen(
    viewModel: UpdateHabitTaskViewModel,
    onBack: () -> Unit,
    onTaskUpdated: (HabitTaskDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.updatedTask) {
        state.updatedTask?.let { task ->
            onTaskUpdated(task)
            viewModel.consumeUpdatedTask()
        }
    }

    if (state.showConflictDialog) {
        LulErrorAlertDialog(
            title = stringResource(R.string.update_task_conflict_title),
            message = stringResource(R.string.update_task_conflict_message),
            dismissText = stringResource(R.string.update_task_conflict_dismiss),
            onDismiss = viewModel::dismissConflictDialog,
            retryText = stringResource(R.string.update_task_conflict_reload),
            onRetry = {
                viewModel.dismissConflictDialog()
                viewModel.loadTask()
            },
            retryTestTag = UpdateHabitTaskTestTags.CONFLICT_RELOAD,
            errorTestTag = UpdateHabitTaskTestTags.CONFLICT_DIALOG,
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        bottomBar = {
            if (!state.isLoading && state.loadError == null) {
                HabitTaskFormBottomBar(
                    isSubmitting = state.isSubmitting,
                    enabled = !state.isSubmitting,
                    submitLabelRes = R.string.update_task_submit,
                    onSubmit = viewModel::submit,
                )
            }
        },
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = PurplePrimary, modifier = Modifier.size(44.dp))
            }

            state.loadError != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
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

            else -> HabitTaskFormContent(
                form = state.form,
                contentPadding = innerPadding,
                onBack = onBack,
                showTemplates = false,
                showHabitPicker = false,
                showDisciplinePicker = true,
                showCategoryPicker = false,
                taskDisciplines = state.disciplines,
                isTaskDisciplinesLoading = state.isDisciplinesLoading,
                onDisciplineSelected = viewModel::onDisciplineChange,
                onHabitSelected = {},
                onTitleChange = viewModel::onTitleChange,
                onDescriptionChange = viewModel::onDescriptionChange,
                onDifficultyChange = viewModel::onDifficultyChange,
                onFrequencyChange = viewModel::onFrequencyChange,
                onPeriodLengthChange = viewModel::onPeriodLengthChange,
                onPeriodUnitChange = viewModel::onPeriodUnitChange,
                onStartDateChange = viewModel::onStartDateChange,
                onCompletionCriteriaChange = viewModel::onCompletionCriteriaChange,
                onRepetitionsChange = viewModel::onRepetitionsChange,
                onMeasurementUnitChange = viewModel::onMeasurementUnitChange,
                onEvidenceChange = viewModel::onEvidenceChange,
                onPartialAllowedChange = viewModel::onPartialAllowedChange,
                onApplyTemplate = {},
                onDismissError = viewModel::dismissSubmitError,
                preservedStartDate = state.originalStartDate,
            )
        }
    }
}
