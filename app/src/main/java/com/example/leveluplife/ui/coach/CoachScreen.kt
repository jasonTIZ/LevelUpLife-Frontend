package com.example.leveluplife.ui.coach

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leveluplife.R
import com.example.leveluplife.data.coach.ChatMessage
import com.example.leveluplife.ui.theme.DarkBackground
import com.example.leveluplife.ui.theme.DarkOnBackground
import com.example.leveluplife.ui.theme.DarkOnSurfaceVariant
import com.example.leveluplife.ui.theme.DarkSurfaceVariant
import com.example.leveluplife.ui.theme.PurplePrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachScreen(
    viewModel: CoachViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val totalItems = state.messages.size + if (state.isLoading) 1 else 0
    LaunchedEffect(totalItems) {
        if (totalItems > 0) listState.animateScrollToItem(totalItems - 1)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.coach_title),
                            color = DarkOnBackground,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            stringResource(R.string.coach_subtitle),
                            color = DarkOnSurfaceVariant,
                            fontSize = 11.sp,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = DarkOnBackground,
                        )
                    }
                },
                actions = {
                    if (state.messages.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearHistory) {
                            Icon(
                                Icons.Filled.DeleteSweep,
                                contentDescription = stringResource(R.string.coach_clear_history),
                                tint = DarkOnSurfaceVariant,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (state.messages.isEmpty() && !state.isLoading) {
                    item {
                        EmptyState(
                            onSuggestionClick = { suggestion ->
                                viewModel.onInputChange(suggestion)
                                viewModel.sendMessage()
                            },
                        )
                    }
                }

                items(state.messages) { msg ->
                    MessageBubble(msg)
                }

                if (state.isLoading) {
                    item { TypingIndicator() }
                }

                if (state.error != null) {
                    item {
                        ErrorBanner(
                            message = state.error!!,
                            onDismiss = viewModel::dismissError,
                        )
                    }
                }
            }

            ChatInput(
                text = state.inputText,
                onTextChange = viewModel::onInputChange,
                onSend = viewModel::sendMessage,
                onDismissKeyboard = { keyboardController?.hide() },
                enabled = !state.isLoading,
            )
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage) {
    val timeStr = remember(msg.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
    }

    if (msg.isFromUser) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = msg.text,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(18.dp, 4.dp, 18.dp, 18.dp))
                    .background(PurplePrimary)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            )
            Spacer(Modifier.height(2.dp))
            Text(timeStr, color = DarkOnSurfaceVariant, fontSize = 10.sp)
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.Bottom) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PurplePrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Psychology,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = msg.text,
                    color = DarkOnBackground,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .clip(RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = timeStr,
                color = DarkOnSurfaceVariant,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 40.dp),
            )
        }
    }
}

@Composable
private fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")
    Row(verticalAlignment = Alignment.Bottom) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(PurplePrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Psychology, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp))
                .background(DarkSurfaceVariant)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            repeat(3) { index ->
                val alpha by transition.animateFloat(
                    initialValue = 0.25f,
                    targetValue = 1f,
                    animationSpec = InfiniteRepeatableSpec(
                        animation = tween(durationMillis = 500),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(index * 180),
                    ),
                    label = "dot_$index",
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(DarkOnSurfaceVariant.copy(alpha = alpha)),
                )
            }
        }
    }
}

@Composable
private fun EmptyState(onSuggestionClick: (String) -> Unit) {
    val suggestions = listOf(
        stringResource(R.string.coach_suggestion_1),
        stringResource(R.string.coach_suggestion_2),
        stringResource(R.string.coach_suggestion_3),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(PurplePrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Psychology,
                contentDescription = null,
                tint = PurplePrimary,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.coach_empty_title),
            color = DarkOnBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.coach_welcome),
            color = DarkOnSurfaceVariant,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        suggestions.forEach { suggestion ->
            Surface(
                onClick = { onSuggestionClick(suggestion) },
                shape = RoundedCornerShape(24.dp),
                color = DarkSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = suggestion,
                    color = DarkOnSurfaceVariant,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Text("✕", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onDismissKeyboard: () -> Unit,
    enabled: Boolean,
) {
    val canSend = enabled && text.isNotBlank()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .onKeyEvent { event ->
                        when {
                            event.key == Key.Enter && !event.isShiftPressed && event.type == KeyEventType.KeyDown && canSend -> {
                                onSend()
                                true
                            }
                            event.key == Key.Escape && event.type == KeyEventType.KeyDown -> {
                                onDismissKeyboard()
                                true
                            }
                            else -> false
                        }
                    },
                placeholder = {
                    Text(
                        stringResource(R.string.coach_input_hint),
                        color = DarkOnSurfaceVariant,
                        fontSize = 14.sp,
                    )
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send,
                    capitalization = KeyboardCapitalization.Sentences,
                ),
                keyboardActions = KeyboardActions(
                    onSend = { if (canSend) onSend() },
                ),
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    focusedTextColor = DarkOnBackground,
                    unfocusedTextColor = DarkOnBackground,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = PurplePrimary,
                ),
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { if (canSend) onSend() },
                enabled = canSend,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (canSend) PurplePrimary else DarkSurfaceVariant),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.coach_send_button),
                    tint = if (canSend) Color.White else DarkOnSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.coach_keyboard_hint),
            color = DarkOnSurfaceVariant.copy(alpha = 0.45f),
            fontSize = 10.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}
