package com.example.leveluplife.ui.createtask

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.leveluplife.R
import com.example.leveluplife.domain.validation.HabitTaskFieldError

@Composable
fun HabitTaskFieldError.toMessage(): String = when (this) {
    HabitTaskFieldError.Required -> stringResource(R.string.field_required)
    is HabitTaskFieldError.TooShort -> stringResource(R.string.field_min_length, min)
    is HabitTaskFieldError.TooLong -> stringResource(R.string.field_max_length, max)
    HabitTaskFieldError.InvalidNumber -> stringResource(R.string.create_task_error_invalid_number)
    HabitTaskFieldError.CriteriaRequired -> stringResource(R.string.create_task_error_criteria_required)
    HabitTaskFieldError.EvidenceRequired -> stringResource(R.string.create_task_error_evidence_required)
}
