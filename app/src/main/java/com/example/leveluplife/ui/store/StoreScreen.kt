package com.example.leveluplife.ui.store

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leveluplife.R
import com.example.leveluplife.data.network.dto.RewardItemDto

internal val GoldColor = Color(0xFFF59E0B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    viewModel: StoreViewModel,
    playerGold: Int,
    onBack: () -> Unit,
    onOpenInventory: () -> Unit,
    onItemClick: (RewardItemDto) -> Unit = {},
    onPurchaseSuccess: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val successTemplate = stringResource(R.string.store_buy_success)
    val errorInsufficient = stringResource(R.string.store_purchase_error_insufficient)
    val errorGeneric = stringResource(R.string.store_purchase_error_generic)

    LaunchedEffect(state.purchaseSuccessName) {
        val name = state.purchaseSuccessName ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(successTemplate.replace("%1\$s", name))
        onPurchaseSuccess()
        viewModel.clearPurchaseSuccess()
    }

    LaunchedEffect(state.buyError) {
        val key = state.buyError ?: return@LaunchedEffect
        val msg = if (key == "insufficient_gold") errorInsufficient else errorGeneric
        snackbarHostState.showSnackbar(msg)
        viewModel.clearBuyError()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.store_title),
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.store_back))
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = GoldColor,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.store_player_gold, playerGold),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = GoldColor,
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = onOpenInventory) {
                                Icon(
                                    Icons.Filled.Backpack,
                                    contentDescription = stringResource(R.string.backpack_open_cd),
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchChange,
                    placeholder = { Text(stringResource(R.string.store_search_hint), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = if (state.searchQuery.isNotEmpty()) {
                        { IconButton(onClick = { viewModel.onSearchChange("") }) { Icon(Icons.Filled.Close, contentDescription = null) } }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    ),
                )
            }
        },
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            state.error != null -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.store_error_generic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { viewModel.load() }) {
                        Text(stringResource(R.string.store_retry))
                    }
                }
            }

            state.groupedItems.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.store_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            else -> LazyColumn(
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 24.dp,
                    start = 16.dp,
                    end = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.groupedItems.forEach { (typeName, groupItems) ->
                    item(key = "header_$typeName") {
                        SectionHeader(typeName = typeName, typeId = groupItems.firstOrNull()?.typeId)
                    }
                    items(groupItems, key = { it.id }) { item ->
                        RewardItemCard(
                            item = item,
                            playerGold = playerGold,
                            isBuying = item.id == state.buyingItemId,
                            anyBuying = state.buyingItemId != null,
                            onBuy = { viewModel.purchase(item) },
                            onClick = { onItemClick(item) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(typeName: String, typeId: Int?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = iconForTypeId(typeId),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = typeName.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RewardItemCard(
    item: RewardItemDto,
    playerGold: Int,
    isBuying: Boolean,
    anyBuying: Boolean,
    onBuy: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cost = item.costGold.toInt()
    val canAfford = playerGold >= cost

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ItemImage(itemId = item.id, typeId = item.typeId, name = item.name)

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!item.description.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = GoldColor,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = stringResource(R.string.store_cost_gold, cost),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldColor,
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = onBuy,
                enabled = canAfford && !isBuying && !anyBuying && item.isActive,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(36.dp)
                    .width(if (isBuying) 48.dp else 88.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                ),
            ) {
                if (isBuying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.store_buy_button),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemImage(itemId: Int, typeId: Int?, name: String) {
    val drawableRes = drawableForItemId(itemId)
    val sizeMod = Modifier
        .size(72.dp)
        .clip(RoundedCornerShape(10.dp))

    if (drawableRes != null) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = sizeMod,
        )
    } else {
        Box(
            modifier = sizeMod.background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = iconForTypeId(typeId),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}

@DrawableRes
internal fun drawableForItemId(id: Int): Int? = when (id) {
    1 -> R.drawable.escudo_1
    2 -> R.drawable.escudo_2
    3 -> R.drawable.escudo_3
    4 -> R.drawable.potenciacion_1
    5 -> R.drawable.potenciacion_2
    6 -> R.drawable.potenciacion_3
    7 -> R.drawable.mejora_1
    8 -> R.drawable.mejora_2
    9 -> R.drawable.mejora_3
    10 -> R.drawable.mejora_4
    else -> null
}

internal fun iconForTypeId(typeId: Int?): ImageVector = when (typeId) {
    1 -> Icons.Filled.Shield
    2 -> Icons.Filled.Bolt
    3 -> Icons.Filled.AddCircle
    4 -> Icons.Filled.Healing
    else -> Icons.Filled.Star
}
