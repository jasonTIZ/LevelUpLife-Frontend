package com.example.leveluplife.ui.createtask

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.example.leveluplife.R
import com.example.leveluplife.domain.validation.HabitTaskFieldError
import com.example.leveluplife.ui.components.LulErrorAlertDialog

@Composable
fun HabitTaskFieldError.toMessage(): String = when (this) {
    HabitTaskFieldError.Required -> stringResource(R.string.field_required)
    is HabitTaskFieldError.TooShort -> stringResource(R.string.field_min_length, min)
    is HabitTaskFieldError.TooLong -> stringResource(R.string.field_max_length, max)
    HabitTaskFieldError.InvalidNumber -> stringResource(R.string.create_task_error_invalid_number)
    HabitTaskFieldError.InvalidDate -> stringResource(R.string.create_task_error_invalid_date)
    HabitTaskFieldError.PastDate -> stringResource(R.string.create_task_error_past_date)
    HabitTaskFieldError.InvalidOption -> stringResource(R.string.create_task_error_invalid_option)
    HabitTaskFieldError.CriteriaRequired -> stringResource(R.string.create_task_error_criteria_required)
    HabitTaskFieldError.EvidenceRequired -> stringResource(R.string.create_task_error_evidence_required)
}

@Composable
fun HabitTaskFormState.firstValidationErrorMessage(): String? {
    if (!showValidationErrors) return null
    return listOfNotNull(
        fieldErrors.habitId?.toMessage(),
        fieldErrors.title?.toMessage(),
        fieldErrors.difficulty?.toMessage(),
        fieldErrors.frequency?.toMessage(),
        fieldErrors.periodLength?.toMessage(),
        fieldErrors.periodUnit?.toMessage(),
        fieldErrors.startDate?.toMessage(),
        fieldErrors.completionCriteria?.toMessage(),
        fieldErrors.repetitions?.toMessage(),
        fieldErrors.measurementUnit?.toMessage(),
        fieldErrors.evidence?.toMessage(),
        fieldErrors.timerSeconds?.toMessage(),
    ).firstOrNull()
}

@Composable
fun HabitTaskFormErrorDialog(
    form: HabitTaskFormState,
    onDismiss: () -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }
    val activeMessage = form.submitError ?: form.firstValidationErrorMessage()

    LaunchedEffect(activeMessage) {
        if (activeMessage != null) {
            dialogMessage = activeMessage
            showDialog = true
        } else {
            showDialog = false
            dialogMessage = null
        }
    }

    if (showDialog && dialogMessage != null) {
        LulErrorAlertDialog(
            title = stringResource(R.string.error_dialog_title),
            message = dialogMessage ?: "",
            dismissText = stringResource(R.string.login_dismiss),
            onDismiss = {
                showDialog = false
                onDismiss()
            },
            titleTextStyle = MaterialTheme.typography.titleLarge,
            messageTextStyle = MaterialTheme.typography.bodyMedium,
            iconTint = MaterialTheme.colorScheme.error,
        )
    }
}
