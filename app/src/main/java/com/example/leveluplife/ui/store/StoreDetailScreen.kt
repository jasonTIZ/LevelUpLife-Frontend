package com.example.leveluplife.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.leveluplife.R
import com.example.leveluplife.data.network.dto.RewardItemDto

private val SuccessGreen = Color(0xFF22C55E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreen(
    item: RewardItemDto,
    viewModel: StoreDetailViewModel,
    isPurchased: Boolean = false,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val successMsg = stringResource(R.string.store_purchase_success)

    LaunchedEffect(state.purchaseSuccess) {
        if (state.purchaseSuccess) {
            snackbarHostState.showSnackbar(successMsg)
            viewModel.clearPurchaseResult()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.store_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            ItemHeroImage(itemId = item.id, typeId = item.typeId, imageUrl = item.imageUrl, name = item.name)

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                if (!item.isActive) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.store_unavailable_badge),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .background(Color(0xFFEF4444), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                Spacer(Modifier.height(16.dp))

                DetailRow(label = stringResource(R.string.store_detail_cost)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Bolt, contentDescription = null, tint = GoldColor, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.store_cost_gold, item.costGold.toInt()),
                            fontWeight = FontWeight.Bold,
                            color = GoldColor,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                if (item.effectValue != null) {
                    Spacer(Modifier.height(12.dp))
                    DetailRow(label = stringResource(R.string.store_detail_effect)) {
                        Text(
                            text = stringResource(R.string.store_detail_effect_value, item.effectValue),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }

                if (!item.typeName.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    DetailRow(label = stringResource(R.string.store_detail_type)) {
                        Text(
                            text = item.typeName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.store_detail_description_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.description ?: stringResource(R.string.store_detail_no_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (item.description != null) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    lineHeight = 22.sp,
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.purchase(item) },
                    enabled = item.isActive && !state.isPurchasing && !isPurchased && !state.purchaseSuccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    ),
                ) {
                    if (state.isPurchasing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else if (state.purchaseSuccess) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.store_purchase_success_button), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    } else if (isPurchased) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.store_owned_badge), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    } else {
                        Icon(Icons.Filled.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (item.isActive) {
                                stringResource(R.string.store_detail_redeem_button)
                            } else {
                                stringResource(R.string.store_detail_redeem_disabled)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        )
                    }
                }

                if (state.purchaseError != null) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEF4444).copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = if (state.purchaseError == "not_found") {
                                stringResource(R.string.store_purchase_error_not_found)
                            } else {
                                stringResource(R.string.store_purchase_error_insufficient)
                            },
                            color = Color(0xFFEF4444),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ItemHeroImage(itemId: Int, typeId: Int?, imageUrl: String?, name: String) {
    val heroMod = Modifier
        .fillMaxWidth()
        .height(220.dp)
        .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))

    val localDrawable = drawableForItemId(itemId)
    when {
        localDrawable != null -> Image(
            painter = painterResource(localDrawable),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = heroMod,
        )
        !imageUrl.isNullOrBlank() -> AsyncImage(
            model = imageUrl,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = heroMod,
        )
        else -> Box(
            modifier = heroMod.background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = iconForTypeId(typeId),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp),
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}
