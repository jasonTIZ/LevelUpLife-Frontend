package com.example.leveluplife.ui.createtask

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.leveluplife.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class LabeledOption(val value: String, @StringRes val labelRes: Int)

object CreateHabitTaskOptions {
    val completionCriteria = listOf(
        LabeledOption("REPETITIONS", R.string.create_task_option_criteria_repetitions),
        LabeledOption("TIMER", R.string.create_task_option_criteria_timer),
        LabeledOption("EVIDENCE", R.string.create_task_option_criteria_evidence),
    )
    val measurementUnits = listOf(
        LabeledOption("REPS", R.string.create_task_option_unit_reps),
        LabeledOption("SERIES", R.string.create_task_option_unit_series),
        LabeledOption("KMS", R.string.create_task_option_unit_km),
        LabeledOption("CALS", R.string.create_task_option_unit_cals),
    )
    val evidenceTypes = listOf(
        LabeledOption("PHOTO", R.string.create_task_option_evidence_photo),
        LabeledOption("VIDEO", R.string.create_task_option_evidence_video),
        LabeledOption("HEALTH_CONNECT", R.string.create_task_option_evidence_health),
    )
    val difficulties = listOf(
        LabeledOption("EASY", R.string.create_task_option_difficulty_easy),
        LabeledOption("MEDIUM", R.string.create_task_option_difficulty_medium),
        LabeledOption("HARD", R.string.create_task_option_difficulty_hard),
        LabeledOption("EPIC", R.string.create_task_option_difficulty_epic),
    )
    val frequencies = listOf(
        LabeledOption("DAILY", R.string.create_task_option_frequency_daily),
        LabeledOption("WEEKLY", R.string.create_task_option_frequency_weekly),
        LabeledOption("MONTHLY", R.string.create_task_option_frequency_monthly),
    )
    val periodUnits = listOf(
        LabeledOption("DAYS", R.string.create_task_option_period_days),
        LabeledOption("WEEKS", R.string.create_task_option_period_weeks),
        LabeledOption("MONTHS", R.string.create_task_option_period_months),
    )

    @Composable
    fun resolveLabel(options: List<LabeledOption>, value: String?): String =
        options.firstOrNull { it.value == value }?.labelRes?.let { stringResource(it) }
            ?: value.orEmpty()

    fun formatStartDate(isoDate: String): String = runCatching {
        val date = LocalDate.parse(isoDate.trim())
        date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES")))
    }.getOrElse { isoDate.ifBlank { "—" } }

    @Composable
    fun previewGoalLine(
        completionCriteria: String?,
        repetitions: String,
        measurementUnit: String?,
        evidence: String?,
    ): String = when (completionCriteria) {
        "REPETITIONS" -> {
            val count = repetitions.ifBlank { "—" }
            val unit = resolveLabel(measurementUnits, measurementUnit).ifBlank { "—" }
            "$count $unit"
        }
        "EVIDENCE" -> resolveLabel(evidenceTypes, evidence).ifBlank { "—" }
        "TIMER" -> stringResource(R.string.create_task_option_criteria_timer)
        else -> "—"
    }
}
