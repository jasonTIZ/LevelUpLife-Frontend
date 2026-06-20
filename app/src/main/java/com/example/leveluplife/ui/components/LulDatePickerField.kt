package com.example.leveluplife.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.leveluplife.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LulDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    hint: String?,
    isError: Boolean,
    modifier: Modifier = Modifier,
    preservedIsoDate: String? = null,
    fieldColors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
) {
    var showPicker by remember { mutableStateOf(false) }
    val displayValue = remember(value) { formatIsoDateForDisplay(value) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = label,
            isError = isError,
            trailingIcon = {
                IconButton(onClick = { showPicker = true }) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = stringResource(R.string.cd_open_date_picker),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            colors = fieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .clickableWithoutRipple { showPicker = true },
        )

        when {
            !hint.isNullOrBlank() -> {
                Text(
                    text = hint,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }

    if (showPicker) {
        LulDatePickerDialog(
            currentValue = value,
            preservedIsoDate = preservedIsoDate,
            onDismiss = { showPicker = false },
            onConfirm = { selected ->
                onValueChange(selected)
                showPicker = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LulDatePickerDialog(
    currentValue: String,
    preservedIsoDate: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val today = remember { LocalDate.now() }
    val preservedDate = remember(preservedIsoDate) {
        preservedIsoDate
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    }
    val initialMillis = remember(currentValue, today) {
        parseIsoDateToMillis(currentValue)
            ?: today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = millisToLocalDate(utcTimeMillis)
                if (preservedDate != null && date == preservedDate) return true
                return !date.isBefore(today)
            }
        },
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onConfirm(formatIsoDate(millis))
                    }
                },
            ) {
                Text(stringResource(R.string.register_date_picker_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_cancel))
            }
        },
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = false,
        )
    }
}

private fun formatIsoDateForDisplay(isoDate: String): String = runCatching {
    LocalDate.parse(isoDate.trim())
        .format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES")))
}.getOrElse { isoDate.trim() }

private fun parseIsoDateToMillis(value: String): Long? = runCatching {
    LocalDate.parse(value.trim())
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
}.getOrNull()

private fun formatIsoDate(millis: Long): String =
    millisToLocalDate(millis).format(DateTimeFormatter.ISO_LOCAL_DATE)

private fun millisToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

@Composable
private fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick,
    )
}
