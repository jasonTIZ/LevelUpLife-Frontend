package com.example.leveluplife.ui.evidence

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.leveluplife.R
import com.example.leveluplife.data.network.dto.EvidenceDto
import com.example.leveluplife.health.HealthConnectManager
import com.example.leveluplife.health.HealthEvidencePayload
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Locale
import com.example.leveluplife.ui.components.LulErrorAlertDialog
import com.example.leveluplife.ui.components.showLulSnackbar
import com.example.leveluplife.ui.theme.DarkBackground
import com.example.leveluplife.ui.theme.DarkOnBackground
import com.example.leveluplife.ui.theme.DarkOnSurfaceVariant
import com.example.leveluplife.ui.theme.DarkSurfaceVariant
import com.example.leveluplife.ui.theme.PurplePrimary

private val DangerRed = Color(0xFFCF6679)
private val HealthJson = Json { ignoreUnknownKeys = true }

private fun formatHealthValue(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else String.format(Locale.US, "%.2f", value)

@Composable
private fun healthEvidenceLabel(healthDataJson: String): String {
    val payload = remember(healthDataJson) {
        runCatching { HealthJson.decodeFromString<HealthEvidencePayload>(healthDataJson) }.getOrNull()
    } ?: return healthDataJson
    val value = formatHealthValue(payload.value)
    return when (payload.metric) {
        "STEPS" -> stringResource(R.string.evidence_health_value_steps, value)
        "DISTANCE" -> stringResource(R.string.evidence_health_value_distance, value)
        "CALORIES" -> stringResource(R.string.evidence_health_value_calories, value)
        "EXERCISE" -> stringResource(R.string.evidence_health_value_exercise, value)
        else -> stringResource(R.string.evidence_health_value_generic, value, payload.unit)
    }
}

@Composable
fun EvidenceGalleryScreen(
    viewModel: EvidenceGalleryViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            val mimeType = context.contentResolver.getType(uri)
            viewModel.uploadEvidence(uri.toString(), mimeType)
        }
    }

    var showMetricPicker by remember { mutableStateOf(false) }
    var pendingMetric by remember { mutableStateOf<HealthConnectManager.Metric?>(null) }
    val healthUnavailableMessage = stringResource(R.string.evidence_health_unavailable)
    val healthPermissionDeniedMessage = stringResource(R.string.evidence_health_permission_denied)

    val healthPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
    ) { granted ->
        val metric = pendingMetric
        pendingMetric = null
        if (metric != null && granted.containsAll(viewModel.healthPermissions)) {
            viewModel.syncHealthMetric(metric)
        } else {
            scope.launch { snackbarHostState.showLulSnackbar(healthPermissionDeniedMessage) }
        }
    }

    val onPickMetric: (HealthConnectManager.Metric) -> Unit = { metric ->
        showMetricPicker = false
        pendingMetric = metric
        healthPermissionLauncher.launch(viewModel.healthPermissions)
    }

    val deleteSuccessMessage = stringResource(R.string.evidence_gallery_delete_success)
    val successMessage = stringResource(R.string.evidence_upload_success)
    val errorGenericMessage = stringResource(R.string.evidence_upload_error_generic)
    val errorInvalidMessage = stringResource(R.string.evidence_upload_error_invalid)
    val errorFileMessage = stringResource(R.string.evidence_upload_error_file)
    val errorTaskNotFoundMessage = stringResource(R.string.evidence_gallery_error_not_found)
    val errorHealthReadMessage = stringResource(R.string.evidence_health_read_failed)

    LaunchedEffect(state.deleteSuccess) {
        if (state.deleteSuccess) {
            snackbarHostState.showLulSnackbar(deleteSuccessMessage)
            viewModel.onDeleteSuccessShown()
        }
    }

    LaunchedEffect(state.uploadSuccess) {
        if (state.uploadSuccess) {
            snackbarHostState.showLulSnackbar(successMessage)
            viewModel.onUploadSuccessShown()
        }
    }

    LaunchedEffect(state.uploadError) {
        val error = state.uploadError ?: return@LaunchedEffect
        val message = when {
            error == "cannot_read_file" -> errorFileMessage
            error.startsWith("upload_invalid_fields") -> errorInvalidMessage
            error == "task_not_found" -> errorTaskNotFoundMessage
            error == "health_read_failed" -> errorHealthReadMessage
            else -> "$errorGenericMessage ($error)"
        }
        snackbarHostState.showLulSnackbar(message, durationMillis = 5_000L)
        viewModel.onUploadErrorShown()
    }

    if (state.pendingDeleteEvidence != null) {
        DeleteEvidenceConfirmDialog(
            state = state,
            onDismiss = viewModel::cancelDelete,
            onConfirm = viewModel::confirmDelete,
            onAcknowledgedChange = viewModel::onDeleteAcknowledgedChange,
        )
    }

    if (state.deleteError != null) {
        val forbiddenMessage = stringResource(R.string.evidence_gallery_delete_error_forbidden)
        val notFoundMessage = stringResource(R.string.evidence_gallery_delete_error_not_found)
        val taskCompletedMessage = stringResource(R.string.evidence_gallery_delete_error_task_completed)
        val genericMessage = stringResource(R.string.evidence_gallery_delete_error_generic)
        val errorMessage = when (state.deleteError) {
            "forbidden" -> forbiddenMessage
            "not_found" -> notFoundMessage
            "task_completed" -> taskCompletedMessage
            else -> genericMessage
        }
        LulErrorAlertDialog(
            title = stringResource(R.string.error_dialog_title),
            message = errorMessage,
            dismissText = stringResource(R.string.login_dismiss),
            onDismiss = viewModel::dismissDeleteError,
        )
    }

    if (showMetricPicker) {
        AlertDialog(
            onDismissRequest = { showMetricPicker = false },
            title = { Text(stringResource(R.string.evidence_health_pick_title)) },
            text = {
                Column {
                    TextButton(
                        onClick = { onPickMetric(HealthConnectManager.Metric.STEPS) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.evidence_health_metric_steps)) }
                    TextButton(
                        onClick = { onPickMetric(HealthConnectManager.Metric.DISTANCE) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.evidence_health_metric_distance)) }
                    TextButton(
                        onClick = { onPickMetric(HealthConnectManager.Metric.CALORIES) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.evidence_health_metric_calories)) }
                    TextButton(
                        onClick = { onPickMetric(HealthConnectManager.Metric.EXERCISE) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.evidence_health_metric_exercise)) }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMetricPicker = false }) {
                    Text(stringResource(R.string.evidence_health_cancel))
                }
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (state.isUploading) return@FloatingActionButton
                    if (viewModel.isHealthConnectTask) {
                        if (viewModel.isHealthConnectAvailable) {
                            showMetricPicker = true
                        } else {
                            scope.launch { snackbarHostState.showLulSnackbar(healthUnavailableMessage) }
                        }
                    } else {
                        pickImage.launch(viewModel.galleryMimeType)
                    }
                },
                containerColor = PurplePrimary,
                contentColor = DarkOnBackground,
            ) {
                if (state.isUploading) {
                    CircularProgressIndicator(
                        color = DarkOnBackground,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp),
                    )
                } else {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.evidence_upload_button),
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.create_task_back),
                        tint = DarkOnBackground,
                    )
                }
                Text(
                    text = stringResource(R.string.evidence_gallery_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkOnBackground,
                    modifier = Modifier.weight(1f),
                )
            }

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PurplePrimary)
                    }
                }

                state.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = if (state.error == "task_not_found")
                                    stringResource(R.string.evidence_gallery_error_not_found)
                                else
                                    stringResource(R.string.evidence_gallery_error_generic),
                                color = DarkOnSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            TextButton(onClick = viewModel::load) {
                                Text(
                                    stringResource(R.string.evidence_gallery_retry),
                                    color = PurplePrimary,
                                )
                            }
                        }
                    }
                }

                state.evidences.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.evidence_gallery_empty),
                            color = DarkOnSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.evidences, key = { it.id }) { evidence ->
                            EvidenceCard(
                                evidence = evidence,
                                onDelete = { viewModel.requestDelete(evidence) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EvidenceCard(
    evidence: EvidenceDto,
    onDelete: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
    ) {
        Column {
            if (!evidence.url.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(evidence.url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .background(DarkBackground),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.BrokenImage,
                                contentDescription = null,
                                tint = DarkOnSurfaceVariant,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(DarkBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.MonitorHeart,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }

            Column(modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 8.dp, bottom = 4.dp)) {
                Text(
                    text = stringResource(
                        R.string.evidence_gallery_uploaded_on,
                        evidence.uploadedAt.take(10),
                    ),
                    fontSize = 11.sp,
                    color = DarkOnSurfaceVariant,
                )
                if (!evidence.healthDataJson.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = healthEvidenceLabel(evidence.healthDataJson),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkOnBackground,
                        maxLines = 2,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.evidence_gallery_delete_button),
                            tint = DangerRed,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteEvidenceConfirmDialog(
    state: EvidenceGalleryUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onAcknowledgedChange: (Boolean) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = DangerRed,
            )
        },
        title = {
            Text(
                text = stringResource(R.string.evidence_gallery_delete_confirm_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.evidence_gallery_delete_confirm_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Checkbox(
                        checked = state.deleteAcknowledged,
                        onCheckedChange = onAcknowledgedChange,
                    )
                    Text(
                        text = stringResource(R.string.evidence_gallery_delete_irreversible_ack),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.evidence_gallery_delete_cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = state.deleteAcknowledged && !state.isDeleting,
            ) {
                Text(
                    text = if (state.isDeleting)
                        stringResource(R.string.evidence_gallery_delete_confirming)
                    else
                        stringResource(R.string.evidence_gallery_delete_confirm_button),
                    color = if (state.deleteAcknowledged) DangerRed else DarkOnSurfaceVariant,
                )
            }
        },
    )
}
