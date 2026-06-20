package com.example.leveluplife.ui.habit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.leveluplife.R
import com.example.leveluplife.ui.components.CategoryChipsRow
import com.example.leveluplife.ui.components.LulPrimaryButton
import com.example.leveluplife.ui.createtask.HabitTaskEmbeddedForm
import kotlinx.coroutines.delay

@Composable
fun CreateHabitScreen(
    viewModel: CreateHabitViewModel,
    onHabitCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            delay(1500)
            onHabitCreated()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Crear hábito",
                style = MaterialTheme.typography.headlineMedium,
            )

            HabitFormSection(
                uiState = uiState,
                onTitleChange = { viewModel.setTitle(it) },
                onDescriptionChange = { viewModel.setDescription(it) },
                onCategorySelected = { viewModel.setCategoryId(it) },
            )

            Divider()

            Text(
                text = "Tareas",
                style = MaterialTheme.typography.headlineSmall,
            )

            uiState.tasks.forEachIndexed { index, taskForm ->
                HabitTaskEmbeddedForm(
                    form = taskForm,
                    taskNumber = index + 1,
                    canRemove = uiState.tasks.size > 1,
                    onRemove = { viewModel.removeTask(index) },
                    taskDisciplines = uiState.disciplines,
                    isTaskDisciplinesLoading = uiState.isDisciplinesLoading,
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

            Button(
                onClick = { viewModel.addTask() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Text(text = "Agregar tarea")
            }

            LulPrimaryButton(
                text = if (uiState.isLoading) "Creando..." else "Crear hábito",
                onClick = { viewModel.createHabit() },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                isLoading = uiState.isLoading,
            )

            uiState.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun HabitFormSection(
    uiState: CreateHabitUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategorySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CategoryChipsRow(
            categories = uiState.categories,
            selectedId = uiState.selectedCategoryId,
            isLoading = uiState.isCategoriesLoading,
            onSelect = onCategorySelected,
        )

        Text(
            text = stringResource(R.string.create_habit_title_section_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
        )

        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = { Text("Título del hábito") },
            placeholder = { Text("Ej. Ejercicio diario") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.habitTitleError != null,
            supportingText = uiState.habitTitleError?.let { error ->
                { Text(error, color = MaterialTheme.colorScheme.error) }
            },
            singleLine = true,
        )

        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            label = { Text("Descripción (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
        )
    }
}
