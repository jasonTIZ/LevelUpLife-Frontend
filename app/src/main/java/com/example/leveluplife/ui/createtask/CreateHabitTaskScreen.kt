package com.example.leveluplife.ui.createtask

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.leveluplife.R
import com.example.leveluplife.data.network.dto.HabitCategoryDto
import com.example.leveluplife.data.network.dto.HabitDisciplineDto
import com.example.leveluplife.data.network.dto.HabitDto
import com.example.leveluplife.data.network.dto.HabitTaskDto
import com.example.leveluplife.domain.validation.HabitTaskFieldError
import com.example.leveluplife.ui.components.TimerPreview
import com.example.leveluplife.ui.components.CategoryChipsRow
import com.example.leveluplife.ui.components.DisciplineChipsRow
import com.example.leveluplife.ui.components.LulDatePickerField
import com.example.leveluplife.ui.components.LulErrorAlertDialog
import com.example.leveluplife.ui.components.SelectableChip

object CreateHabitTaskTestTags {
    const val SUBMIT_BUTTON = "create_task_submit_button"
    const val PREVIEW_CARD = "create_task_preview_card"
}

private val SectionSpacing = 16.dp
private val FieldSpacing = 12.dp
private val ScreenHorizontalPadding = 20.dp

@Composable
fun CreateHabitTaskScreen(
    viewModel: CreateHabitTaskViewModel,
    onBack: () -> Unit,
    onTaskCreated: (HabitTaskDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.createdTask) {
        val task = state.createdTask ?: return@LaunchedEffect
        if (!task.aiDifficultyFailed) {
            onTaskCreated(task)
            viewModel.consumeCreatedTask()
        }
    }

    state.createdTask?.takeIf { it.aiDifficultyFailed }?.let { task ->
        LulErrorAlertDialog(
            title = stringResource(R.string.ai_difficulty_failed_title),
            message = stringResource(R.string.ai_difficulty_failed_message),
            dismissText = stringResource(R.string.login_dismiss),
            onDismiss = {
                onTaskCreated(task)
                viewModel.consumeCreatedTask()
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!state.isLoadingHabits && state.habitsLoadError == null) {
                HabitTaskFormBottomBar(
                    isSubmitting = state.isSubmitting,
                    enabled = !state.isSubmitting,
                    submitLabelRes = R.string.create_task_submit,
                    onSubmit = viewModel::submit,
                )
            }
        },
    ) { innerPadding ->
        when {
            state.isLoadingHabits -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
            }

            state.habitsLoadError != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.habitsLoadError ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = viewModel::loadHabits) {
                        Text(stringResource(R.string.create_task_retry), color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            else -> HabitTaskFormContent(
                form = state.form,
                contentPadding = innerPadding,
                onBack = onBack,
                showTemplates = true,
                showHabitPicker = true,
                onHabitSelected = viewModel::onHabitSelected,
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
                onTimerSecondsDefinedChange = viewModel::onTimerSecondsDefinedChange,
                onTimerSecondsLongChange = viewModel::onTimerSecondsLongChange,
                onTimerPauseAllowedChange = viewModel::onTimerPauseAllowedChange,
                onApplyTemplate = viewModel::applyTemplate,
                onDismissError = viewModel::dismissSubmitError,
            )
        }
    }
}

@Composable
internal fun HabitTaskFormBottomBar(
    isSubmitting: Boolean,
    enabled: Boolean,
    submitLabelRes: Int,
    onSubmit: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = ScreenHorizontalPadding, vertical = 12.dp),
        ) {
            Button(
                onClick = onSubmit,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag(CreateHabitTaskTestTags.SUBMIT_BUTTON),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(submitLabelRes),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateHabitTaskHeader(onBack: () -> Unit, isEditMode: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.create_task_back),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        Column(modifier = Modifier.padding(top = 8.dp, end = ScreenHorizontalPadding)) {
            Text(
                text = stringResource(
                    if (isEditMode) R.string.update_task_title else R.string.create_task_title,
                ),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(
                    if (isEditMode) R.string.update_task_subtitle else R.string.create_task_subtitle,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HabitTaskFormContent(
    form: HabitTaskFormState,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    showTemplates: Boolean,
    showHabitPicker: Boolean,
    onHabitSelected: (Int) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDifficultyChange: (String) -> Unit,
    onFrequencyChange: (String) -> Unit,
    onPeriodLengthChange: (String) -> Unit,
    onPeriodUnitChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onCompletionCriteriaChange: (String) -> Unit,
    onRepetitionsChange: (String) -> Unit,
    onMeasurementUnitChange: (String) -> Unit,
    onEvidenceChange: (String) -> Unit,
    onPartialAllowedChange: (Boolean) -> Unit,
    onTimerSecondsDefinedChange: (String) -> Unit,
    onTimerSecondsLongChange: (String) -> Unit,
    onTimerPauseAllowedChange: (Boolean) -> Unit,
    onApplyTemplate: (TaskFormTemplate) -> Unit,
    onDismissError: () -> Unit,
    showDisciplinePicker: Boolean = false,
    showCategoryPicker: Boolean = false,
    taskCategories: List<HabitCategoryDto> = emptyList(),
    selectedCategoryId: Int? = null,
    isCategoriesLoading: Boolean = false,
    onCategorySelected: (Int) -> Unit = {},
    taskDisciplines: List<HabitDisciplineDto> = emptyList(),
    isTaskDisciplinesLoading: Boolean = false,
    onDisciplineSelected: (Int) -> Unit = {},
    preservedStartDate: String? = null,
) {
    val scroll = rememberScrollState()
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
        focusedContainerColor = MaterialTheme.colorScheme.background,
        unfocusedContainerColor = MaterialTheme.colorScheme.background,
        focusedTextColor = MaterialTheme.colorScheme.onBackground,
        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
        focusedLabelColor = MaterialTheme.colorScheme.secondary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        cursorColor = MaterialTheme.colorScheme.primary,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding())
            .verticalScroll(scroll)
            .padding(horizontal = ScreenHorizontalPadding)
            .padding(bottom = contentPadding.calculateBottomPadding() + 88.dp),
        verticalArrangement = Arrangement.spacedBy(SectionSpacing),
    ) {
        CreateHabitTaskHeader(onBack = onBack, isEditMode = !showTemplates)

        if (showTemplates) {
            QuickTemplatesRow(
                selectedTemplateId = form.selectedTemplateId,
                onApplyTemplate = onApplyTemplate,
            )
        }

        if (showDisciplinePicker) {
            if (showCategoryPicker) {
                CategoryChipsRow(
                    categories = taskCategories,
                    selectedId = selectedCategoryId,
                    isLoading = isCategoriesLoading,
                    onSelect = onCategorySelected,
                )
            }
            DisciplineChipsRow(
                disciplines = taskDisciplines,
                selectedId = form.selectedDisciplineId,
                isLoading = isTaskDisciplinesLoading,
                onSelect = onDisciplineSelected,
            )
        }

        FormSection(title = stringResource(R.string.create_task_section_basic)) {
            if (showHabitPicker) {
                HabitDropdown(
                    habits = form.habits,
                    selectedHabit = form.selectedHabit,
                    error = if (form.showValidationErrors) form.fieldErrors.habitId else null,
                    onSelected = onHabitSelected,
                    fieldColors = fieldColors,
                )
            } else {
                ReadOnlyMetaField(
                    label = stringResource(R.string.create_task_field_habit),
                    value = form.selectedHabit?.title?.ifBlank { "—" } ?: "—",
                    fieldColors = fieldColors,
                )
            }

            if (!showCategoryPicker) {
                val categoryLabel = form.selectedHabit?.categoryName?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.create_task_category_unknown)
                ReadOnlyMetaField(
                    label = stringResource(R.string.create_task_field_category),
                    value = categoryLabel,
                    fieldColors = fieldColors,
                )
            }

            OutlinedTextField(
                value = form.title,
                onValueChange = onTitleChange,
                label = { Text(stringResource(R.string.create_task_field_title)) },
                modifier = Modifier.fillMaxWidth(),
                isError = form.showValidationErrors && form.fieldErrors.title != null,
                supportingText = {
                    if (form.showValidationErrors && form.fieldErrors.title != null) {
                        Text(form.fieldErrors.title!!.toMessage())
                    }
                },
                colors = fieldColors,
                singleLine = true,
            )

            OutlinedTextField(
                value = form.description,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(R.string.create_task_field_description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                colors = fieldColors,
            )
        }

        FormSection(title = stringResource(R.string.create_task_section_goal)) {
            // Criteria type is editable in both create and edit (#36): the user can switch it.
            LabeledOptionDropdown(
                label = stringResource(R.string.create_task_field_criteria_type),
                options = CreateHabitTaskOptions.completionCriteria,
                selected = form.completionCriteria,
                error = if (form.showValidationErrors) form.fieldErrors.completionCriteria else null,
                onSelected = onCompletionCriteriaChange,
                fieldColors = fieldColors,
            )

            if (form.completionCriteria == "REPETITIONS") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FieldSpacing),
                ) {
                    OutlinedTextField(
                        value = form.repetitions,
                        onValueChange = onRepetitionsChange,
                        label = { Text(stringResource(R.string.create_task_field_repetitions)) },
                        modifier = Modifier.weight(1f),
                        isError = form.showValidationErrors &&
                            (form.fieldErrors.repetitions != null ||
                                form.fieldErrors.measurementUnit != null),
                        supportingText = {
                            val err = form.fieldErrors.repetitions
                                ?: form.fieldErrors.measurementUnit
                            if (form.showValidationErrors && err != null) Text(err.toMessage())
                        },
                        colors = fieldColors,
                        singleLine = true,
                    )
                    LabeledOptionDropdown(
                        label = stringResource(R.string.create_task_field_unit),
                        options = CreateHabitTaskOptions.measurementUnits,
                        selected = form.measurementUnit,
                        error = null,
                        onSelected = onMeasurementUnitChange,
                        fieldColors = fieldColors,
                        modifier = Modifier.weight(1f),
                    )
                }

                PartialAllowedRow(
                    checked = form.isPartialAllowed,
                    onCheckedChange = onPartialAllowedChange,
                )
            }

            if (form.completionCriteria == "EVIDENCE") {
                LabeledOptionDropdown(
                    label = stringResource(R.string.create_task_field_evidence),
                    options = CreateHabitTaskOptions.evidenceTypes,
                    selected = form.evidence,
                    error = if (form.showValidationErrors) form.fieldErrors.evidence else null,
                    onSelected = onEvidenceChange,
                    fieldColors = fieldColors,
                )
            }

            if (form.completionCriteria == "TIMER") {
                TimerCriteriaFields(
                    form = form,
                    onTimerSecondsDefinedChange = onTimerSecondsDefinedChange,
                    onTimerSecondsLongChange = onTimerSecondsLongChange,
                    onTimerPauseAllowedChange = onTimerPauseAllowedChange,
                    fieldColors = fieldColors,
                )
                if (!showTemplates) {
                    TimerPropagationNotice()
                }
            }
        }

        FormSection(title = stringResource(R.string.create_task_section_planning)) {
            LabeledOptionDropdown(
                label = stringResource(R.string.create_task_field_frequency),
                options = CreateHabitTaskOptions.frequencies,
                selected = form.frequency,
                error = if (form.showValidationErrors) form.fieldErrors.frequency else null,
                onSelected = onFrequencyChange,
                fieldColors = fieldColors,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FieldSpacing),
            ) {
                OutlinedTextField(
                    value = form.periodLength,
                    onValueChange = onPeriodLengthChange,
                    label = { Text(stringResource(R.string.create_task_field_period_length)) },
                    modifier = Modifier.weight(1f),
                    isError = form.showValidationErrors && form.fieldErrors.periodLength != null,
                    supportingText = {
                        if (form.showValidationErrors && form.fieldErrors.periodLength != null) {
                            Text(form.fieldErrors.periodLength!!.toMessage())
                        }
                    },
                    colors = fieldColors,
                    singleLine = true,
                )
                LabeledOptionDropdown(
                    label = stringResource(R.string.create_task_field_period_unit),
                    options = CreateHabitTaskOptions.periodUnits,
                    selected = form.periodUnit,
                    error = if (form.showValidationErrors) form.fieldErrors.periodUnit else null,
                    onSelected = onPeriodUnitChange,
                    fieldColors = fieldColors,
                    modifier = Modifier.weight(1f),
                )
            }

            LulDatePickerField(
                value = form.startDate,
                onValueChange = onStartDateChange,
                label = { Text(stringResource(R.string.create_task_field_start_date)) },
                hint = stringResource(
                    if (showTemplates) {
                        R.string.create_task_start_date_hint
                    } else {
                        R.string.update_task_start_date_hint
                    },
                ),
                isError = form.showValidationErrors && form.fieldErrors.startDate != null,
                preservedIsoDate = preservedStartDate,
                fieldColors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.create_task_section_preview),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            TaskPreviewCard(
                form = form,
                modifier = Modifier.testTag(CreateHabitTaskTestTags.PREVIEW_CARD),
            )
        }

        HabitTaskFormErrorDialog(form = form, onDismiss = onDismissError)

        Spacer(Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HabitTaskEmbeddedForm(
    form: HabitTaskFormState,
    taskNumber: Int,
    canRemove: Boolean,
    onRemove: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDifficultyChange: (String) -> Unit,
    onFrequencyChange: (String) -> Unit,
    onPeriodLengthChange: (String) -> Unit,
    onPeriodUnitChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onCompletionCriteriaChange: (String) -> Unit,
    onRepetitionsChange: (String) -> Unit,
    onMeasurementUnitChange: (String) -> Unit,
    onEvidenceChange: (String) -> Unit,
    onPartialAllowedChange: (Boolean) -> Unit,
    onTimerSecondsDefinedChange: (String) -> Unit,
    onTimerSecondsLongChange: (String) -> Unit,
    onTimerPauseAllowedChange: (Boolean) -> Unit,
    onApplyTemplate: (TaskFormTemplate) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
    taskDisciplines: List<HabitDisciplineDto>? = null,
    isTaskDisciplinesLoading: Boolean = false,
    onDisciplineSelected: ((Int) -> Unit)? = null,
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
        focusedContainerColor = MaterialTheme.colorScheme.background,
        unfocusedContainerColor = MaterialTheme.colorScheme.background,
        focusedTextColor = MaterialTheme.colorScheme.onBackground,
        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
        focusedLabelColor = MaterialTheme.colorScheme.secondary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        cursorColor = MaterialTheme.colorScheme.primary,
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SectionSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Tarea $taskNumber",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (canRemove) {
                TextButton(onClick = onRemove) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        QuickTemplatesRow(
            selectedTemplateId = form.selectedTemplateId,
            onApplyTemplate = onApplyTemplate,
        )

        if (taskDisciplines != null && onDisciplineSelected != null) {
            DisciplineChipsRow(
                disciplines = taskDisciplines,
                selectedId = form.selectedDisciplineId,
                isLoading = isTaskDisciplinesLoading,
                onSelect = onDisciplineSelected,
            )
        }

        FormSection(title = stringResource(R.string.create_task_section_basic)) {
            OutlinedTextField(
                value = form.title,
                onValueChange = onTitleChange,
                label = { Text(stringResource(R.string.create_task_field_title)) },
                modifier = Modifier.fillMaxWidth(),
                isError = form.showValidationErrors && form.fieldErrors.title != null,
                supportingText = {
                    if (form.showValidationErrors && form.fieldErrors.title != null) {
                        Text(form.fieldErrors.title!!.toMessage())
                    }
                },
                colors = fieldColors,
                singleLine = true,
            )

            OutlinedTextField(
                value = form.description,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(R.string.create_task_field_description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                colors = fieldColors,
            )
        }

        FormSection(title = stringResource(R.string.create_task_section_goal)) {
            LabeledOptionDropdown(
                label = stringResource(R.string.create_task_field_criteria_type),
                options = CreateHabitTaskOptions.completionCriteria,
                selected = form.completionCriteria,
                error = if (form.showValidationErrors) form.fieldErrors.completionCriteria else null,
                onSelected = onCompletionCriteriaChange,
                fieldColors = fieldColors,
            )

            if (form.completionCriteria == "REPETITIONS") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FieldSpacing),
                ) {
                    OutlinedTextField(
                        value = form.repetitions,
                        onValueChange = onRepetitionsChange,
                        label = { Text(stringResource(R.string.create_task_field_repetitions)) },
                        modifier = Modifier.weight(1f),
                        isError = form.showValidationErrors &&
                            (form.fieldErrors.repetitions != null || form.fieldErrors.measurementUnit != null),
                        supportingText = {
                            val err = form.fieldErrors.repetitions ?: form.fieldErrors.measurementUnit
                            if (form.showValidationErrors && err != null) Text(err.toMessage())
                        },
                        colors = fieldColors,
                        singleLine = true,
                    )
                    LabeledOptionDropdown(
                        label = stringResource(R.string.create_task_field_unit),
                        options = CreateHabitTaskOptions.measurementUnits,
                        selected = form.measurementUnit,
                        error = null,
                        onSelected = onMeasurementUnitChange,
                        fieldColors = fieldColors,
                        modifier = Modifier.weight(1f),
                    )
                }

                PartialAllowedRow(
                    checked = form.isPartialAllowed,
                    onCheckedChange = onPartialAllowedChange,
                )
            }

            if (form.completionCriteria == "EVIDENCE") {
                LabeledOptionDropdown(
                    label = stringResource(R.string.create_task_field_evidence),
                    options = CreateHabitTaskOptions.evidenceTypes,
                    selected = form.evidence,
                    error = if (form.showValidationErrors) form.fieldErrors.evidence else null,
                    onSelected = onEvidenceChange,
                    fieldColors = fieldColors,
                )
            }

            if (form.completionCriteria == "TIMER") {
                TimerCriteriaFields(
                    form = form,
                    onTimerSecondsDefinedChange = onTimerSecondsDefinedChange,
                    onTimerSecondsLongChange = onTimerSecondsLongChange,
                    onTimerPauseAllowedChange = onTimerPauseAllowedChange,
                    fieldColors = fieldColors,
                )
            }
        }

        FormSection(title = stringResource(R.string.create_task_section_planning)) {
            LabeledOptionDropdown(
                label = stringResource(R.string.create_task_field_frequency),
                options = CreateHabitTaskOptions.frequencies,
                selected = form.frequency,
                error = if (form.showValidationErrors) form.fieldErrors.frequency else null,
                onSelected = onFrequencyChange,
                fieldColors = fieldColors,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FieldSpacing),
            ) {
                OutlinedTextField(
                    value = form.periodLength,
                    onValueChange = onPeriodLengthChange,
                    label = { Text(stringResource(R.string.create_task_field_period_length)) },
                    modifier = Modifier.weight(1f),
                    isError = form.showValidationErrors && form.fieldErrors.periodLength != null,
                    supportingText = {
                        if (form.showValidationErrors && form.fieldErrors.periodLength != null) {
                            Text(form.fieldErrors.periodLength!!.toMessage())
                        }
                    },
                    colors = fieldColors,
                    singleLine = true,
                )
                LabeledOptionDropdown(
                    label = stringResource(R.string.create_task_field_period_unit),
                    options = CreateHabitTaskOptions.periodUnits,
                    selected = form.periodUnit,
                    error = if (form.showValidationErrors) form.fieldErrors.periodUnit else null,
                    onSelected = onPeriodUnitChange,
                    fieldColors = fieldColors,
                    modifier = Modifier.weight(1f),
                )
            }

            LulDatePickerField(
                value = form.startDate,
                onValueChange = onStartDateChange,
                label = { Text(stringResource(R.string.create_task_field_start_date)) },
                hint = stringResource(R.string.create_task_start_date_hint),
                isError = form.showValidationErrors && form.fieldErrors.startDate != null,
                fieldColors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.create_task_section_preview),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            TaskPreviewCard(form = form)
        }

        HabitTaskFormErrorDialog(form = form, onDismiss = onDismissError)
    }
}

@Composable
private fun QuickTemplatesRow(
    selectedTemplateId: String?,
    onApplyTemplate: (TaskFormTemplate) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.create_task_templates_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TaskFormTemplates.all.forEach { template ->
                val selected = selectedTemplateId == template.id
                SelectableChip(
                    label = template.label,
                    selected = selected,
                    onClick = { onApplyTemplate(template) },
                )
            }
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(FieldSpacing),
                content = content,
            )
        }
    }
}

@Composable
private fun ReadOnlyMetaField(
    label: String,
    value: String,
    fieldColors: androidx.compose.material3.TextFieldColors,
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        colors = fieldColors,
        singleLine = true,
    )
}

@Composable
private fun PartialAllowedRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.create_task_partial_allowed),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f).padding(end = 8.dp),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
    }
}

@Composable
internal fun TimerCriteriaFields(
    form: HabitTaskFormState,
    onTimerSecondsDefinedChange: (String) -> Unit,
    onTimerSecondsLongChange: (String) -> Unit,
    onTimerPauseAllowedChange: (Boolean) -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors,
) {
    val seconds = form.timerSecondsDefined.toIntOrNull()

    OutlinedTextField(
        value = form.timerSecondsDefined,
        onValueChange = onTimerSecondsDefinedChange,
        label = { Text(stringResource(R.string.create_task_field_timer_seconds)) },
        modifier = Modifier.fillMaxWidth(),
        isError = form.showValidationErrors && form.fieldErrors.timerSeconds != null,
        supportingText = {
            val err = form.fieldErrors.timerSeconds
            when {
                form.showValidationErrors && err != null -> Text(err.toMessage())
                seconds != null && seconds > 0 -> Text(
                    text = stringResource(R.string.create_task_timer_minutes_format, formatMinutes(seconds)),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodySmall,
                )
                else -> Text(
                    text = stringResource(R.string.create_task_timer_seconds_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        colors = fieldColors,
        singleLine = true,
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.create_task_timer_examples_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TimerExampleChip(R.string.create_task_timer_example_1min, "60", form.timerSecondsDefined, onTimerSecondsDefinedChange)
            TimerExampleChip(R.string.create_task_timer_example_5min, "300", form.timerSecondsDefined, onTimerSecondsDefinedChange)
            TimerExampleChip(R.string.create_task_timer_example_25min, "1500", form.timerSecondsDefined, onTimerSecondsDefinedChange)
        }
    }

    OutlinedTextField(
        value = form.timerSecondsLong,
        onValueChange = onTimerSecondsLongChange,
        label = { Text(stringResource(R.string.create_task_field_timer_threshold)) },
        modifier = Modifier.fillMaxWidth(),
        isError = form.showValidationErrors && form.fieldErrors.timerLong != null,
        supportingText = {
            val err = form.fieldErrors.timerLong
            if (form.showValidationErrors && err != null) {
                Text(err.toMessage())
            } else {
                Text(
                    text = stringResource(R.string.create_task_timer_threshold_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        colors = fieldColors,
        singleLine = true,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.create_task_timer_pause_label),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f).padding(end = 8.dp),
        )
        Switch(
            checked = form.timerPauseAllowed,
            onCheckedChange = onTimerPauseAllowedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
    }

    val previewSeconds = form.timerSecondsDefined.toIntOrNull()
    if (previewSeconds != null && previewSeconds >= 1) {
        var showPreview by remember { mutableStateOf(false) }
        TextButton(onClick = { showPreview = !showPreview }) {
            Text(
                text = stringResource(
                    if (showPreview) R.string.timer_preview_hide else R.string.timer_preview_show,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (showPreview) {
            TimerPreview(
                durationSeconds = previewSeconds,
                pauseAllowed = form.timerPauseAllowed,
                thresholdSeconds = form.timerSecondsLong.toIntOrNull()?.takeIf { it > 0 },
            )
        }
    }
}

@Composable
private fun TimerExampleChip(
    labelRes: Int,
    seconds: String,
    currentValue: String,
    onSelect: (String) -> Unit,
) {
    val selected = currentValue == seconds
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val backgroundColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
    Surface(
        onClick = { onSelect(seconds) },
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Text(
            text = stringResource(labelRes),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
        )
    }
}

private fun formatMinutes(totalSeconds: Int): String {
    val minutes = totalSeconds / 60.0
    return if (minutes == minutes.toLong().toDouble()) {
        minutes.toLong().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", minutes)
    }
}

@Composable
private fun TimerPropagationNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(R.string.update_task_timer_propagation_notice),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun TaskPreviewCard(form: HabitTaskFormState, modifier: Modifier = Modifier) {
    val habitTitle = form.selectedHabit?.title?.ifBlank { "—" } ?: "—"
    val taskTitle = form.title.ifBlank { "—" }
    val description = form.description.ifBlank {
        stringResource(R.string.create_task_preview_no_description)
    }
    val goalLine = CreateHabitTaskOptions.previewGoalLine(
        completionCriteria = form.completionCriteria,
        repetitions = form.repetitions,
        measurementUnit = form.measurementUnit,
        evidence = form.evidence,
        timerSecondsDefined = form.timerSecondsDefined,
    )
    val frequencyLine = CreateHabitTaskOptions.resolveLabel(
        CreateHabitTaskOptions.frequencies,
        form.frequency,
    ).ifBlank { "—" }
    val startLine = CreateHabitTaskOptions.formatStartDate(form.startDate)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = habitTitle,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = taskTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "\"$description\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(4.dp))

            PreviewInfoRow(
                icon = { Icon(Icons.Outlined.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                label = stringResource(R.string.create_task_preview_label_goal),
                value = goalLine,
            )
            PreviewInfoRow(
                icon = { Icon(Icons.Outlined.Repeat, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                label = stringResource(R.string.create_task_preview_label_frequency),
                value = frequencyLine,
            )
            PreviewInfoRow(
                icon = {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                label = stringResource(R.string.create_task_preview_label_start),
                value = startLine,
            )

            if (form.completionCriteria == "REPETITIONS" && form.isPartialAllowed) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Outlined.TaskAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(R.string.create_task_preview_partial_allowed),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewInfoRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
            icon()
        }
        Column {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitDropdown(
    habits: List<HabitDto>,
    selectedHabit: HabitDto?,
    error: HabitTaskFieldError?,
    onSelected: (Int) -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedHabit?.title ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.create_task_field_habit)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            isError = error != null,
            supportingText = { if (error != null) Text(error.toMessage()) },
            colors = fieldColors,
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            habits.forEach { habit ->
                DropdownMenuItem(
                    text = { Text(habit.title) },
                    onClick = {
                        onSelected(habit.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabeledOptionDropdown(
    label: String,
    options: List<LabeledOption>,
    selected: String?,
    error: HabitTaskFieldError?,
    onSelected: (String) -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    var expanded by remember { mutableStateOf(false) }
    val displayValue = CreateHabitTaskOptions.resolveLabel(options, selected)

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            isError = error != null,
            supportingText = { if (error != null) Text(error.toMessage()) },
            colors = fieldColors,
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes)) },
                    onClick = {
                        onSelected(option.value)
                        expanded = false
                    },
                )
            }
        }
    }
}
