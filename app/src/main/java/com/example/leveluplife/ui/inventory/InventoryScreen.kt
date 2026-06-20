package com.example.leveluplife.ui.inventory

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backpack
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leveluplife.R
import com.example.leveluplife.data.network.dto.ActiveEffectDto
import com.example.leveluplife.data.network.dto.PlayerInventoryDto
import com.example.leveluplife.ui.rewards.GoldColor
import com.example.leveluplife.ui.rewards.drawableForItemId
import com.example.leveluplife.ui.rewards.iconForTypeId
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val errorNoQuantity = stringResource(R.string.backpack_activate_error_no_quantity)
    val errorAlreadyActive = stringResource(R.string.backpack_activate_error_already_active)
    val errorRecovery = stringResource(R.string.backpack_activate_error_recovery)
    val errorGeneric = stringResource(R.string.backpack_activate_error_generic)

    LaunchedEffect(state.activateError) {
        val key = state.activateError ?: return@LaunchedEffect
        val msg = when (key) {
            "no_quantity" -> errorNoQuantity
            "effect_already_active" -> errorAlreadyActive
            "recovery_no_target" -> errorRecovery
            else -> errorGeneric
        }
        snackbarHostState.showSnackbar(msg)
        viewModel.clearActivateError()
    }

    LaunchedEffect(state.recoveryMessage) {
        val message = state.recoveryMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearRecoveryMessage()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.inventory_title),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.inventory_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
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
                        text = stringResource(R.string.inventory_error),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { viewModel.load() }) {
                        Text(stringResource(R.string.inventory_retry))
                    }
                }
            }

            state.items.isEmpty() && state.activeEffects.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Backpack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.inventory_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
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
                if (state.activeEffects.isNotEmpty()) {
                    item(key = "effects_header") {
                        SectionLabel(text = stringResource(R.string.backpack_effects_title))
                    }
                    items(state.activeEffects, key = { "effect_${it.id}" }) { effect ->
                        ActiveEffectCard(effect = effect)
                    }
                    item(key = "effects_spacer") {
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (state.items.isNotEmpty()) {
                    item(key = "items_header") {
                        SectionLabel(text = stringResource(R.string.backpack_items_title))
                    }
                    items(state.items, key = { "inv_${it.id}" }) { inventoryItem ->
                        val hasActiveEffect = state.activeEffects.any {
                            it.inventoryId == inventoryItem.id && it.isActive
                        }
                        InventoryItemCard(
                            item = inventoryItem,
                            canActivate = inventoryItem.quantity > 0 && !hasActiveEffect,
                            isActivating = inventoryItem.id == state.activatingItemId,
                            anyActivating = state.activatingItemId != null,
                            onActivate = { viewModel.activate(inventoryItem.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = text,
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

@Composable
private fun ActiveEffectCard(effect: ActiveEffectDto, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = iconForTypeId(effect.rewardItemTypeId),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = effect.rewardItemName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                effect.effectValue?.let { value ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.backpack_effect_multiplier, formatEffectValue(value)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }
                effect.remainingCharges?.let { charges ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.backpack_effect_charges, charges),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                effect.expiresAt?.let { expires ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.backpack_effect_expires, formatExpiresAt(expires)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryItemCard(
    item: PlayerInventoryDto,
    canActivate: Boolean,
    isActivating: Boolean,
    anyActivating: Boolean,
    onActivate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ItemImage(itemId = item.rewardItemId, typeId = item.rewardItemTypeId, name = item.rewardItemName)

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.rewardItemName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!item.rewardItemTypeName.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.rewardItemTypeName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item.effectValue?.let { value ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.backpack_effect_multiplier, formatEffectValue(value)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (item.isEquipped && item.quantity == 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.backpack_equipped_badge),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (item.quantity > 0) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.inventory_quantity, item.quantity),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                if (canActivate) {
                    Button(
                        onClick = onActivate,
                        enabled = !isActivating && !anyActivating,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                        ),
                    ) {
                        if (isActivating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.backpack_activate),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemImage(itemId: Int, typeId: Int?, name: String) {
    val drawableRes = drawableForItemId(itemId)
    val sizeMod = Modifier
        .size(52.dp)
        .clip(RoundedCornerShape(12.dp))

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
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

private fun formatEffectValue(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()

private fun formatExpiresAt(expiresAt: String): String =
    runCatching {
        val instant = Instant.parse(expiresAt)
        DateTimeFormatter.ofPattern("dd/MM HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }.getOrElse {
        expiresAt.take(16).replace('T', ' ')
    }
