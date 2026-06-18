package com.example.leveluplife.ui.habittaskdetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.leveluplife.R
import com.example.leveluplife.data.network.dto.HabitTaskDto
import com.example.leveluplife.data.network.dto.TimerCriteriaDto
import com.example.leveluplife.ui.createtask.CreateHabitTaskOptions
import com.example.leveluplife.ui.createtask.LabeledOption

object HabitTaskLabels {

    private val weekDayCodeToRes = mapOf(
        "MON" to R.string.weekday_mon,
        "TUE" to R.string.weekday_tue,
        "WED" to R.string.weekday_wed,
        "THU" to R.string.weekday_thu,
        "FRI" to R.string.weekday_fri,
        "SAT" to R.string.weekday_sat,
        "SUN" to R.string.weekday_sun,
    )

    private val completionCriteria = CreateHabitTaskOptions.completionCriteria + listOf(
        LabeledOption("TIMER", R.string.create_task_option_criteria_timer),
    )

    @Composable
    fun difficulty(value: String): String =
        CreateHabitTaskOptions.resolveLabel(CreateHabitTaskOptions.difficulties, value)
            .ifBlank { value }

    @Composable
    fun frequency(value: String): String =
        CreateHabitTaskOptions.resolveLabel(CreateHabitTaskOptions.frequencies, value)
            .ifBlank { value }

    @Composable
    fun periodUnit(value: String): String =
        CreateHabitTaskOptions.resolveLabel(CreateHabitTaskOptions.periodUnits, value)
            .ifBlank { value }

    @Composable
    fun completionCriteria(value: String): String =
        CreateHabitTaskOptions.resolveLabel(completionCriteria, value)
            .ifBlank { value }

    @Composable
    fun evidence(value: String): String =
        CreateHabitTaskOptions.resolveLabel(CreateHabitTaskOptions.evidenceTypes, value)
            .ifBlank { value }

    @Composable
    fun periodSummary(periodLength: Int, periodUnit: String): String {
        val unitLabel = when (periodUnit) {
            "DAYS" -> stringResource(
                if (periodLength == 1) R.string.period_unit_day else R.string.period_unit_days,
            )
            "WEEKS" -> stringResource(
                if (periodLength == 1) R.string.period_unit_week else R.string.period_unit_weeks,
            )
            "MONTHS" -> stringResource(
                if (periodLength == 1) R.string.period_unit_month else R.string.period_unit_months,
            )
            else -> periodUnit(periodUnit).lowercase()
        }
        return stringResource(R.string.task_detail_period_value, periodLength, unitLabel)
    }

    @Composable
    fun weekDays(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val codes = raw.split(',')
            .map { it.trim().uppercase() }
            .filter { it.isNotEmpty() }
        val labels = ArrayList<String>(codes.size)
        for (code in codes) {
            labels.add(weekDayLabel(code))
        }
        return labels.joinToString(", ")
    }

    @Composable
    private fun weekDayLabel(code: String): String {
        val labelRes = weekDayCodeToRes[code] ?: return code
        return stringResource(labelRes)
    }

    @Composable
    fun criteriaGoalSummary(task: HabitTaskDto): String = when (task.completionCriteria) {
        "REPETITIONS" -> task.repetitionCriteria?.toReadableSummary()
            ?: stringResource(R.string.task_detail_criteria_missing)
        "EVIDENCE" -> task.evidence?.let { evidence(it) }
            ?: stringResource(R.string.task_detail_criteria_missing)
        "TIMER" -> task.timerCriteria?.let { timerGoal(it) }
            ?: stringResource(R.string.task_detail_criteria_missing)
        else -> stringResource(R.string.task_detail_criteria_missing)
    }

    @Composable
    fun timerGoal(timer: TimerCriteriaDto): String {
        val duration = formatTimerDuration(timer.numSecondsDefined)
        return if (timer.typePauseIsAllowed) {
            stringResource(R.string.task_detail_timer_with_pause, duration)
        } else {
            stringResource(R.string.task_detail_timer_no_pause, duration)
        }
    }

    fun formatTimerDuration(seconds: Int): String {
        if (seconds < 60) return "$seconds s"
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours} h ${minutes} min"
            hours > 0 -> "${hours} h"
            minutes > 0 && secs > 0 -> "${minutes} min ${secs} s"
            minutes > 0 -> "${minutes} min"
            else -> "$secs s"
        }
    }
}
