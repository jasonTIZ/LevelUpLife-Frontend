package com.example.leveluplife.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.leveluplife.R
import com.example.leveluplife.data.network.dto.HabitDisciplineDto

@Composable
fun DisciplineChipsRow(
    disciplines: List<HabitDisciplineDto>,
    selectedId: Int?,
    isLoading: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.create_habit_discipline_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
        )

        when {
            isLoading -> {
                Text(
                    text = stringResource(R.string.create_habit_disciplines_loading),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            disciplines.isEmpty() -> {
                Text(
                    text = stringResource(R.string.create_habit_disciplines_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    disciplines.forEach { discipline ->
                        SelectableChip(
                            label = discipline.name,
                            selected = discipline.id == selectedId,
                            onClick = { onSelect(discipline.id) },
                        )
                    }
                }
            }
        }
    }
}
