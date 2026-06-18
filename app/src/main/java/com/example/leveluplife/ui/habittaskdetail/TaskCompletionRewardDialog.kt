package com.example.leveluplife.ui.habittaskdetail

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.leveluplife.R
import com.example.leveluplife.ui.theme.DarkOnBackground
import com.example.leveluplife.ui.theme.DarkOnSurfaceVariant
import com.example.leveluplife.ui.theme.DarkSurfaceVariant
import com.example.leveluplife.ui.theme.PurplePrimary
import com.example.leveluplife.ui.theme.PurplePrimaryContainer

object TaskCompletionRewardTestTags {
    const val REWARD_DIALOG = "task_completion_reward_dialog"
    const val LEVEL_UP_DIALOG = "task_completion_level_up_dialog"
    const val DISMISS_BUTTON = "task_completion_reward_dismiss"
}

private val GoldAccent = Color(0xFFFFD54F)

@Composable
fun TaskCompletionRewardDialog(
    reward: TaskCompletionReward,
    onDismiss: () -> Unit,
) {
    val scale = remember { Animatable(0.85f) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(reward) {
        scale.animateTo(1f, animationSpec = tween(420, easing = FastOutSlowInEasing))
        progress.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f))
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale.value)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                DarkSurfaceVariant,
                                PurplePrimaryContainer.copy(alpha = 0.35f),
                            ),
                        ),
                        shape = RoundedCornerShape(24.dp),
                    )
                    .padding(24.dp)
                    .testTag(
                        if (reward.leveledUp) {
                            TaskCompletionRewardTestTags.LEVEL_UP_DIALOG
                        } else {
                            TaskCompletionRewardTestTags.REWARD_DIALOG
                        },
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = if (reward.leveledUp) {
                        stringResource(R.string.complete_task_level_up_title)
                    } else {
                        stringResource(R.string.complete_task_reward_title)
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (reward.leveledUp) GoldAccent else PurplePrimary,
                    textAlign = TextAlign.Center,
                )

                if (reward.leveledUp) {
                    Text(
                        text = stringResource(
                            R.string.complete_task_level_up_transition,
                            reward.previousLevel,
                            reward.newLevel,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkOnBackground,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.complete_task_level_up_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkOnSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.complete_task_reward_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkOnSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.complete_task_reward_task_label),
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    color = DarkOnSurfaceVariant,
                )
                Text(
                    text = reward.taskTitle.ifBlank { stringResource(R.string.task_detail_untitled) },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkOnBackground,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = stringResource(R.string.complete_task_xp_earned, reward.xpEarned),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.complete_task_current_level, reward.newLevel),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkOnBackground,
                    )
                    LinearProgressIndicator(
                        progress = { progress.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = PurplePrimary,
                        trackColor = DarkOnSurfaceVariant.copy(alpha = 0.25f),
                    )
                    Text(
                        text = stringResource(R.string.complete_task_progress_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkOnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (reward.streakUpdated) {
                    Text(
                        text = stringResource(R.string.complete_task_streak_updated),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF59E0B),
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag(TaskCompletionRewardTestTags.DISMISS_BUTTON),
                ) {
                    Text(
                        text = stringResource(R.string.complete_task_reward_continue),
                        color = PurplePrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
