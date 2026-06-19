package com.example.leveluplife.ui.evidence

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
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.leveluplife.R
import com.example.leveluplife.data.network.dto.EvidenceDto
import com.example.leveluplife.ui.theme.DarkBackground
import com.example.leveluplife.ui.theme.DarkOnBackground
import com.example.leveluplife.ui.theme.DarkOnSurfaceVariant
import com.example.leveluplife.ui.theme.DarkSurfaceVariant
import com.example.leveluplife.ui.theme.PurplePrimary

@Composable
fun EvidenceGalleryScreen(
    viewModel: EvidenceGalleryViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
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
                            EvidenceCard(evidence = evidence)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EvidenceCard(evidence: EvidenceDto) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
    ) {
        Column {
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

            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
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
                        text = evidence.healthDataJson,
                        fontSize = 10.sp,
                        color = DarkOnSurfaceVariant,
                        maxLines = 2,
                    )
                }
            }
        }
    }
}
