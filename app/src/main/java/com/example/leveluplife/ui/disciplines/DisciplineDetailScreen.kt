package com.example.leveluplife.ui.disciplines

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.NavigateBefore
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leveluplife.R
import com.example.leveluplife.data.network.dto.DisciplineData
import com.example.leveluplife.data.network.dto.HabitData
import com.example.leveluplife.ui.theme.LevelUpLifeTheme

object DisciplineDetailTestTags {
    const val LOADING = "discipline_loading"
    const val NOT_FOUND = "discipline_not_found"
    const val ERROR = "discipline_error"
    const val CONTENT = "discipline_content"
    const val HABITS_LOADING = "discipline_habits_loading"
    const val ADD_HABIT_FAB = "discipline_add_habit_fab"
}

@Composable
fun DisciplineDetailScreen(
    viewModel: DisciplineDetailViewModel,
    onNavigateBack: () -> Unit,
    onCreateHabit: (disciplineId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    DisciplineDetailContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onRetry = { viewModel.load() },
        onPageChange = { viewModel.loadPage(it) },
        onCreateHabit = onCreateHabit,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisciplineDetailContent(
    state: DisciplineDetailUiState,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    onPageChange: (Int) -> Unit,
    onCreateHabit: (disciplineId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = (state as? DisciplineDetailUiState.Success)?.discipline?.name
        ?: stringResource(R.string.discipline_detail_title)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        floatingActionButton = {
            if (state is DisciplineDetailUiState.Success && state.isAdmin) {
                ExtendedFloatingActionButton(
                    onClick = { onCreateHabit(state.discipline.id) },
                    icon = {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                    },
                    text = { Text(stringResource(R.string.discipline_add_habit)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        when (state) {
            is DisciplineDetailUiState.Loading -> LoadingState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            is DisciplineDetailUiState.NotFound -> NotFoundState(
                onNavigateBack = onNavigateBack,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            is DisciplineDetailUiState.Error -> ErrorState(
                message = state.message,
                onRetry = onRetry,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            is DisciplineDetailUiState.Success -> SuccessState(
                state = state,
                onPageChange = onPageChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )
    }
}

@Composable
private fun NotFoundState(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(72.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.discipline_not_found),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.discipline_not_found_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onNavigateBack,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        ) {
            Icon(Icons.Outlined.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.discipline_go_back),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.discipline_error_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (!message.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        ) {
            Text(stringResource(R.string.discipline_retry))
        }
    }
}

@Composable
private fun SuccessState(
    state: DisciplineDetailUiState.Success,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.semantics { contentDescription = state.discipline.name },
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { DisciplineMetadataCard(discipline = state.discipline) }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.discipline_habits_section, state.totalItems),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        if (state.isLoadingHabits) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        } else if (state.habits.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.discipline_no_habits),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(state.habits) { habit ->
                HabitCard(habit = habit)
            }
        }

        if (state.totalPages > 1 && !state.isLoadingHabits) {
            item {
                PaginationRow(
                    currentPage = state.currentPage,
                    totalPages = state.totalPages,
                    onPageChange = onPageChange,
                )
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun DisciplineMetadataCard(
    discipline: DisciplineData,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = discipline.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (!discipline.category.isNullOrBlank()) {
                        Text(
                            text = discipline.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp,
                        )
                    }
                }
            }

            if (!discipline.description.isNullOrBlank()) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = discipline.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                )
            }

            if (!discipline.createdAt.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.discipline_created_at, discipline.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HabitCard(
    habit: HabitData,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (!habit.description.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            val chips = buildList {
                habit.frequency?.let { add(it) }
                habit.difficulty?.let { add(it) }
                if (habit.xpReward > 0) add("+${habit.xpReward} XP")
            }

            if (chips.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    chips.forEach { label ->
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                labelColor = MaterialTheme.colorScheme.primary,
                            ),
                            border = null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaginationRow(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = { onPageChange(currentPage - 1) },
            enabled = currentPage > 1,
        ) {
            Icon(Icons.Outlined.NavigateBefore, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.discipline_page_prev))
        }

        Text(
            text = stringResource(R.string.discipline_page_indicator, currentPage, totalPages),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        TextButton(
            onClick = { onPageChange(currentPage + 1) },
            enabled = currentPage < totalPages,
        ) {
            Text(stringResource(R.string.discipline_page_next))
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Outlined.NavigateNext, contentDescription = null)
        }
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

private val previewDiscipline = DisciplineData(
    id = "1",
    name = "Meditación",
    description = "Práctica diaria de mindfulness para mejorar la concentración, reducir el estrés y desarrollar una mente más clara y enfocada.",
    category = "Bienestar mental",
    createdAt = "2024-01-15",
)

private val previewHabits = listOf(
    HabitData(id = "1", name = "Meditar 10 minutos", description = "Sesión guiada de meditación mindfulness.", frequency = "DIARIO", difficulty = "FÁCIL", xpReward = 50),
    HabitData(id = "2", name = "Respiración 4-7-8", description = "Técnica de respiración para reducir la ansiedad.", frequency = "DIARIO", difficulty = "FÁCIL", xpReward = 30),
    HabitData(id = "3", name = "Journaling reflexivo", description = "Escribir 3 cosas por las que estás agradecido.", frequency = "SEMANAL", difficulty = "MEDIO", xpReward = 80),
)

@Preview(showBackground = true, name = "Success – light")
@Composable
private fun PreviewDisciplineDetailSuccess() {
    LevelUpLifeTheme {
        DisciplineDetailContent(
            state = DisciplineDetailUiState.Success(
                discipline = previewDiscipline,
                habits = previewHabits,
                currentPage = 1,
                totalPages = 2,
                totalItems = 12,
                isLoadingHabits = false,
                isAdmin = true,
            ),
            onNavigateBack = {},
            onRetry = {},
            onPageChange = {},
            onCreateHabit = {},
        )
    }
}

@Preview(showBackground = true, name = "Not Found – light")
@Composable
private fun PreviewDisciplineDetailNotFound() {
    LevelUpLifeTheme {
        DisciplineDetailContent(
            state = DisciplineDetailUiState.NotFound,
            onNavigateBack = {},
            onRetry = {},
            onPageChange = {},
            onCreateHabit = {},
        )
    }
}

@Preview(showBackground = true, name = "Loading – light")
@Composable
private fun PreviewDisciplineDetailLoading() {
    LevelUpLifeTheme {
        DisciplineDetailContent(
            state = DisciplineDetailUiState.Loading,
            onNavigateBack = {},
            onRetry = {},
            onPageChange = {},
            onCreateHabit = {},
        )
    }
}

@Preview(showBackground = true, name = "Error – light")
@Composable
private fun PreviewDisciplineDetailError() {
    LevelUpLifeTheme {
        DisciplineDetailContent(
            state = DisciplineDetailUiState.Error("Sin conexión. Revisa tu red e intenta de nuevo."),
            onNavigateBack = {},
            onRetry = {},
            onPageChange = {},
            onCreateHabit = {},
        )
    }
}
