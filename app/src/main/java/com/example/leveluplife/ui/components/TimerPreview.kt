package com.example.leveluplife.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leveluplife.R
import androidx.compose.ui.res.stringResource

private val TimerGreen = Color(0xFF4CAF50)
private val TimerGold = Color(0xFFF59E0B)

/**
 * Reusable, interactive countdown preview for timer criteria.
 *
 * It is a local simulation only: it never reports completion to the backend. The
 * countdown uses frame-aligned monotonic time ([withFrameNanos]), so it stays
 * accurate regardless of wall-clock or timezone changes on the device.
 *
 * @param durationSeconds base required duration (NUM_SECONDS_DEFINED).
 * @param pauseAllowed whether the pause control is offered (TYPE_PAUSE_IS_ALLOWED).
 * @param thresholdSeconds optional extended goal (NUM_SECONDS_LONG), shown as a chip.
 */
@Composable
fun TimerPreview(
    durationSeconds: Int,
    pauseAllowed: Boolean,
    modifier: Modifier = Modifier,
    thresholdSeconds: Int? = null,
) {
    val totalMs = (durationSeconds.coerceAtLeast(1)) * 1000L
    var remainingMs by remember(totalMs) { mutableLongStateOf(totalMs) }
    var running by remember(totalMs) { mutableStateOf(false) }

    val finished = remainingMs <= 0L
    val started = remainingMs < totalMs || finished

    LaunchedEffect(running, totalMs) {
        if (!running) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (running && remainingMs > 0L) {
            val now = withFrameNanos { it }
            remainingMs = (remainingMs - (now - last) / 1_000_000L).coerceAtLeast(0L)
            last = now
            if (remainingMs <= 0L) running = false
        }
    }

    val fraction = (remainingMs.toFloat() / totalMs).coerceIn(0f, 1f)
    val ringColor by animateColorAsState(
        targetValue = if (finished) TimerGreen else MaterialTheme.colorScheme.primary,
        label = "timerRingColor",
    )
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
            Canvas(modifier = Modifier.fillMaxWidth().size(180.dp)) {
                val stroke = 14.dp.toPx()
                val inset = stroke / 2
                val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
                val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = -360f * fraction,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatClock(((remainingMs + 999L) / 1000L).toInt()),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (finished) TimerGreen else MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = statusLabel(finished, running, started),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (finished) TimerGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        thresholdSeconds?.takeIf { it > 0 }?.let { threshold ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(TimerGold, RoundedCornerShape(50)),
                )
                Text(
                    text = stringResource(R.string.timer_preview_extended_goal, formatClock(threshold)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TimerGold,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            when {
                finished -> Unit
                running -> {
                    if (pauseAllowed) {
                        TimerControlButton(
                            label = stringResource(R.string.timer_preview_pause),
                            icon = { Icon(Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick = { running = false },
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.timer_preview_no_pause),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterVertically),
                        )
                    }
                }
                else -> TimerControlButton(
                    label = if (started) {
                        stringResource(R.string.timer_preview_resume)
                    } else {
                        stringResource(R.string.timer_preview_start)
                    },
                    icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    onClick = { running = true },
                )
            }

            if (started) {
                OutlinedButton(
                    onClick = {
                        running = false
                        remainingMs = totalMs
                    },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.timer_preview_reset))
                }
            }
        }

        Text(
            text = stringResource(R.string.timer_preview_accuracy_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TimerControlButton(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
        ),
    ) {
        icon()
        Spacer(Modifier.width(6.dp))
        Text(label)
    }
}

@Composable
private fun statusLabel(finished: Boolean, running: Boolean, started: Boolean): String = when {
    finished -> stringResource(R.string.timer_preview_done)
    running -> stringResource(R.string.timer_preview_running)
    started -> stringResource(R.string.timer_preview_paused)
    else -> stringResource(R.string.timer_preview_idle)
}

/** Formats whole seconds as H:MM:SS (when >= 1h) or MM:SS. */
fun formatClock(totalSeconds: Int): String {
    val s = totalSeconds.coerceAtLeast(0)
    val hours = s / 3600
    val minutes = (s % 3600) / 60
    val seconds = s % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
