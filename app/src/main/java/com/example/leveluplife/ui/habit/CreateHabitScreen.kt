package com.example.leveluplife.ui.habit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.leveluplife.data.network.dto.MeasurementUnit
import com.example.leveluplife.data.network.dto.TaskCompletionCriteria
import com.example.leveluplife.data.network.dto.TaskDifficulty
import com.example.leveluplife.data.network.dto.TaskEvidence
import com.example.leveluplife.data.network.dto.TaskFrequency
import com.example.leveluplife.data.network.dto.TaskPeriodUnit
import com.example.leveluplife.ui.components.LulPrimaryButton
import kotlinx.coroutines.delay

@Composable
fun CreateHabitScreen(
    viewModel: CreateHabitViewModel,
    onHabitCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedTaskIndex by remember { mutableStateOf(0) }

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
                text = "Create New Habit",
                style = MaterialTheme.typography.headlineMedium
            )

            HabitFormSection(
                uiState = uiState,
                onTitleChange = { viewModel.setTitle(it) },
                onDescriptionChange = { viewModel.setDescription(it) },
                onDisciplineIdChange = { viewModel.setDisciplineId(it) }
            )

            Divider()

            Text(
                text = "Tasks",
                style = MaterialTheme.typography.headlineSmall
            )

            uiState.tasks.forEachIndexed { index, task ->
                TaskCard(
                    task = task,
                    isFirst = index == 0,
                    onRemove = { viewModel.removeTask(index) },
                    onUpdate = { viewModel.updateTask(index, { t -> t }) },
                    onSelectDate = {
                        selectedTaskIndex = index
                        showDatePicker = true
                    },
                    onUpdateRepetitionCriteria = { viewModel.updateRepetitionCriteria(index, { rc -> rc }) }
                )
            }

            Button(
                onClick = { viewModel.addTask() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add task"
                )
                Text(text = "Add Task")
            }

            LulPrimaryButton(
                text = if (uiState.isLoading) "Creating..." else "Create Habit",
                onClick = { viewModel.createHabit(1) },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                isLoading = uiState.isLoading
            )

            uiState.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val date = datePickerState.selectedDateMillis?.let { ms ->
                            java.time.LocalDate.ofInstant(
                                java.time.Instant.ofEpochMilli(ms),
                                java.time.ZoneId.systemDefault()
                            ).toString()
                        }
                        if (date != null) {
                            viewModel.updateTask(selectedTaskIndex) { it.copy(startDate = date) }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun HabitFormSection(
    uiState: CreateHabitUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDisciplineIdChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = { Text("Habit Title") },
            placeholder = { Text("e.g., Exercise Daily") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.error != null,
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            label = { Text("Description (Optional)") },
            placeholder = { Text("Describe your habit...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        OutlinedTextField(
            value = uiState.disciplineId?.toString() ?: "",
            onValueChange = { if (it.isNotEmpty()) onDisciplineIdChange(it.toInt()) },
            label = { Text("Discipline ID") },
            placeholder = { Text("Enter discipline ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: TaskUiState,
    isFirst: Boolean,
    onRemove: () -> Unit,
    onUpdate: (TaskUiState) -> Unit,
    onSelectDate: () -> Unit,
    onUpdateRepetitionCriteria: (RepetitionCriteriaUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Task",
                    style = MaterialTheme.typography.titleMedium
                )
                if (!isFirst) {
                    IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Remove task"
                )
                    }
                }
            }

            OutlinedTextField(
                value = task.title,
                onValueChange = { onUpdate(task.copy(title = it)) },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = task.description,
                onValueChange = { onUpdate(task.copy(description = it)) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            OutlinedTextField(
                value = task.startDate,
                onValueChange = {},
                label = { Text("Start Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectDate() },
                readOnly = true
            )

            OutlinedTextField(
                value = task.periodLength.toString(),
                onValueChange = { if (it.isNotEmpty()) onUpdate(task.copy(periodLength = it.toInt())) },
                label = { Text("Period Length") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            TaskDropdown(
                label = "Period Unit",
                selected = task.periodUnit,
                options = TaskPeriodUnit.values().toList(),
                onSelect = { onUpdate(task.copy(periodUnit = it)) }
            )

            TaskDropdown(
                label = "Difficulty",
                selected = task.difficulty,
                options = TaskDifficulty.values().toList(),
                onSelect = { onUpdate(task.copy(difficulty = it)) }
            )

            TaskDropdown(
                label = "Frequency",
                selected = task.frequency,
                options = TaskFrequency.values().toList(),
                onSelect = { onUpdate(task.copy(frequency = it)) }
            )

            TaskDropdown(
                label = "Completion Criteria",
                selected = task.completionCriteria,
                options = TaskCompletionCriteria.values().toList(),
                onSelect = { onUpdate(task.copy(completionCriteria = it)) }
            )

            if (task.completionCriteria == TaskCompletionCriteria.REPETITIONS) {
                RepetitionCriteriaSection(task.repetitionCriteria, onUpdateRepetitionCriteria)
            }

            if (task.completionCriteria == TaskCompletionCriteria.EVIDENCE) {
                TaskDropdown(
                    label = "Evidence Type",
                    selected = task.evidence?.let { TaskEvidence.valueOf(it) } ?: TaskEvidence.PHOTO,
                    options = TaskEvidence.values().toList(),
                    onSelect = { onUpdate(task.copy(evidence = it.name)) }
                )
            }
        }
    }
}

@Composable
fun RepetitionCriteriaSection(
    repetitionCriteria: RepetitionCriteriaUiState?,
    onUpdate: (RepetitionCriteriaUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    if (repetitionCriteria == null) return

    Column(modifier = modifier.padding(top = 16.dp)) {
        Text(
            text = "Repetition Criteria",
            style = MaterialTheme.typography.titleSmall
        )

        OutlinedTextField(
            value = repetitionCriteria.repetitions.toString(),
            onValueChange = { if (it.isNotEmpty()) onUpdate(repetitionCriteria.copy(repetitions = it.toInt())) },
            label = { Text("Repetitions") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        TaskDropdown(
            label = "Measurement Unit",
            selected = MeasurementUnit.valueOf(repetitionCriteria.measurementUnit),
            options = MeasurementUnit.values().toList(),
            onSelect = { onUpdate(repetitionCriteria.copy(measurementUnit = it.name)) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Allow Partial")
            Checkbox(
                checked = repetitionCriteria.isPartialAllowed ?: false,
                onCheckedChange = { onUpdate(repetitionCriteria.copy(isPartialAllowed = it)) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Enum<T>> TaskDropdown(
    label: String,
    selected: T,
    options: List<T>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected.name.replace("_", " "),
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(option.name.replace("_", " ")) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
