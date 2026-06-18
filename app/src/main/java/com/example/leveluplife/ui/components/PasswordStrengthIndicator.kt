package com.example.leveluplife.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.leveluplife.R

enum class PasswordStrength {
    EMPTY,
    WEAK,
    FAIR,
    STRONG,
}

fun evaluatePasswordStrength(password: String): PasswordStrength {
    val normalized = password.trim()
    if (normalized.isEmpty()) return PasswordStrength.EMPTY
    var score = 0
    if (normalized.length >= 6) score++
    if (normalized.length >= 10) score++
    if (normalized.any { it.isUpperCase() }) score++
    if (normalized.any { it.isDigit() }) score++
    if (normalized.any { !it.isLetterOrDigit() }) score++
    return when {
        score <= 2 -> PasswordStrength.WEAK
        score <= 3 -> PasswordStrength.FAIR
        else -> PasswordStrength.STRONG
    }
}

@Composable
fun PasswordStrengthIndicator(
    password: String,
    modifier: Modifier = Modifier,
) {
    val strength = evaluatePasswordStrength(password)
    if (strength == PasswordStrength.EMPTY) return

    val activeColor = when (strength) {
        PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
        PasswordStrength.FAIR -> MaterialTheme.colorScheme.tertiary
        PasswordStrength.STRONG -> MaterialTheme.colorScheme.primary
        PasswordStrength.EMPTY -> MaterialTheme.colorScheme.outline
    }
    val inactiveColor = MaterialTheme.colorScheme.outlineVariant
    val filledSegments = when (strength) {
        PasswordStrength.WEAK -> 1
        PasswordStrength.FAIR -> 2
        PasswordStrength.STRONG -> 3
        PasswordStrength.EMPTY -> 0
    }
    val labelRes = when (strength) {
        PasswordStrength.WEAK -> R.string.register_password_strength_weak
        PasswordStrength.FAIR -> R.string.register_password_strength_fair
        PasswordStrength.STRONG -> R.string.register_password_strength_strong
        PasswordStrength.EMPTY -> R.string.register_password_strength_weak
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (index < filledSegments) activeColor else inactiveColor),
                )
            }
        }
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
