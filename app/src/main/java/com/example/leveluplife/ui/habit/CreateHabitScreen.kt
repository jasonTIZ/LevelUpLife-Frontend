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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.leveluplife.data.network.dto.HabitDisciplineDto
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
                onDisciplineSelected = { viewModel.setDisciplineId(it) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitFormSection(
    uiState: CreateHabitUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDisciplineSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = { Text("Título del hábito") },
            placeholder = { Text("Ej. Ejercicio diario") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.error != null,
            singleLine = true,
        )

        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            label = { Text("Descripción (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
        )

        DisciplineDropdown(
            disciplines = uiState.disciplines,
            selectedId = uiState.disciplineId,
            isLoading = uiState.isDisciplinesLoading,
            onSelect = onDisciplineSelected,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisciplineDropdown(
    disciplines: List<HabitDisciplineDto>,
    selectedId: Int?,
    isLoading: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = disciplines.firstOrNull { it.id == selectedId }
    val label = when {
        isLoading -> "Cargando disciplinas…"
        disciplines.isEmpty() -> "Sin disciplinas disponibles"
        else -> "Disciplina"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (!isLoading && disciplines.isNotEmpty()) expanded = !expanded },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            trailingIcon = {
                if (!isLoading && disciplines.isNotEmpty()) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            disciplines.forEach { discipline ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(discipline.name) },
                    onClick = {
                        onSelect(discipline.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
