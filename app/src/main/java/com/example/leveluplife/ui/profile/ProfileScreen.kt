package com.example.leveluplife.ui.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.leveluplife.R
import com.example.leveluplife.domain.validation.FieldError
import com.example.leveluplife.domain.validation.ProfileValidators
import com.example.leveluplife.ui.auth.toMessage
import com.example.leveluplife.ui.components.LulErrorAlertDialog
import com.example.leveluplife.ui.components.LulPrimaryButton
import com.example.leveluplife.ui.theme.DarkBackground
import com.example.leveluplife.ui.theme.DarkOnBackground
import com.example.leveluplife.ui.theme.DarkOnSurfaceVariant
import com.example.leveluplife.ui.theme.DarkSurface
import com.example.leveluplife.ui.theme.DarkSurfaceVariant
import com.example.leveluplife.ui.theme.PurplePrimary

object ProfileTestTags {
    const val SCREEN = "profile_screen"
    const val EDIT_BUTTON = "profile_edit_button"
    const val SAVE_BUTTON = "profile_save_button"
    const val CANCEL_BUTTON = "profile_cancel_button"
    const val AVATAR_BUTTON = "profile_avatar_button"
    const val LOGOUT_BUTTON = "profile_logout_button"
    const val LOGOUT_CONFIRM_DIALOG = "profile_logout_confirm_dialog"
    const val LOGOUT_CONFIRM_BUTTON = "profile_logout_confirm_button"
    const val NAME_FIELD = "profile_name_field"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val successMessage = stringResource(R.string.profile_save_success)

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri)
        val sizeBytes = resolver.openInputStream(uri)?.use { it.available().toLong() } ?: 0L
        viewModel.onAvatarSelected(uri.toString(), mimeType, sizeBytes)
    }

    LaunchedEffect(state.profileSaved, successMessage) {
        if (state.profileSaved) {
            snackbarHostState.showSnackbar(successMessage)
            viewModel.consumeSavedEvent()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(ProfileTestTags.SCREEN),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = PurplePrimary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.profile_back),
                        tint = DarkOnBackground,
                    )
                }
                Text(
                    text = stringResource(R.string.profile_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkOnBackground,
                )
            }

            Text(
                text = if (state.isEditing) {
                    stringResource(R.string.profile_edit_subtitle)
                } else {
                    stringResource(R.string.profile_subtitle)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = DarkOnSurfaceVariant,
            )

            AvatarSection(
                avatarUri = state.pendingAvatarUri ?: state.avatarUri,
                avatarError = if (state.isEditing) state.avatarError else null,
                serverError = if (state.isEditing) state.serverFieldErrors[ProfileFormField.AVATAR] else null,
                editable = state.isEditing,
                onPickAvatar = {
                    avatarPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
            )

            if (state.className.isNotBlank()) {
                Text(
                    text = stringResource(R.string.profile_class_label) + ": ${state.className}",
                    color = DarkOnSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.profile_level_label, state.level),
                    color = DarkOnSurfaceVariant,
                )
            }

            if (state.isEditing) {
                ProfileEditForm(state = state, viewModel = viewModel)
            } else {
                ProfileViewCard(state = state)
                LulPrimaryButton(
                    text = stringResource(R.string.profile_edit),
                    onClick = viewModel::onStartEditing,
                    modifier = Modifier.testTag(ProfileTestTags.EDIT_BUTTON),
                )
            }

            TextButton(
                onClick = { showLogoutConfirm = true },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag(ProfileTestTags.LOGOUT_BUTTON),
            ) {
                Text(stringResource(R.string.profile_logout), color = PurplePrimary)
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            modifier = Modifier.testTag(ProfileTestTags.LOGOUT_CONFIRM_DIALOG),
            title = { Text(stringResource(R.string.profile_logout_confirm_title)) },
            text = { Text(stringResource(R.string.profile_logout_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    },
                    modifier = Modifier.testTag(ProfileTestTags.LOGOUT_CONFIRM_BUTTON),
                ) {
                    Text(stringResource(R.string.profile_logout_confirm), color = PurplePrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text(stringResource(R.string.profile_cancel_edit))
                }
            },
        )
    }

    if (state.bannerError != null) {
        val message = mapBannerMessage(state.bannerError!!)
        LulErrorAlertDialog(
            title = stringResource(R.string.error_dialog_title),
            message = message,
            dismissText = stringResource(R.string.login_dismiss),
            onDismiss = viewModel::dismissError,
            retryText = if (state.bannerError == "network") stringResource(R.string.profile_retry) else null,
            onRetry = if (state.bannerError == "network") viewModel::loadProfile else null,
        )
    }
}

@Composable
private fun ColumnScope.ProfileEditForm(
    state: ProfileUiState,
    viewModel: ProfileViewModel,
) {
    ProfileTextField(
        value = state.name,
        onValueChange = viewModel::onNameChange,
        label = stringResource(R.string.profile_name_label),
        error = state.nameError,
        serverError = state.serverFieldErrors[ProfileFormField.NAME],
        testTag = ProfileTestTags.NAME_FIELD,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            autoCorrectEnabled = true,
        ),
    )
    ProfileTextField(
        value = state.lastName,
        onValueChange = viewModel::onLastNameChange,
        label = stringResource(R.string.profile_last_name_label),
        error = state.lastNameError,
        serverError = state.serverFieldErrors[ProfileFormField.LAST_NAME],
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            autoCorrectEnabled = true,
        ),
    )
    ProfileTextField(
        value = state.email,
        onValueChange = viewModel::onEmailChange,
        label = stringResource(R.string.profile_email_label),
        error = state.emailError,
        serverError = state.serverFieldErrors[ProfileFormField.EMAIL],
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            autoCorrectEnabled = false,
        ),
    )
    ProfileTextField(
        value = state.userName,
        onValueChange = viewModel::onUserNameChange,
        label = stringResource(R.string.profile_username_label),
        error = state.userNameError,
        serverError = state.serverFieldErrors[ProfileFormField.USER_NAME],
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            autoCorrectEnabled = false,
        ),
    )
    ProfileTextField(
        value = state.birthdate,
        onValueChange = viewModel::onBirthdateChange,
        label = stringResource(R.string.profile_birthdate_label),
        placeholder = stringResource(R.string.profile_birthdate_hint),
        error = state.birthdateError,
        serverError = state.serverFieldErrors[ProfileFormField.BIRTHDATE],
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            autoCorrectEnabled = false,
        ),
    )
    ProfileTextField(
        value = state.bio,
        onValueChange = viewModel::onBioChange,
        label = stringResource(R.string.profile_bio_label),
        placeholder = stringResource(R.string.profile_bio_hint),
        error = state.bioError,
        serverError = state.serverFieldErrors[ProfileFormField.BIO],
        minLines = 3,
        maxLength = ProfileValidators.BIO_MAX,
    )

    LulPrimaryButton(
        text = if (state.isSaving) {
            stringResource(R.string.profile_saving)
        } else {
            stringResource(R.string.profile_save)
        },
        onClick = viewModel::onSubmit,
        enabled = !state.isSaving,
        isLoading = state.isSaving,
        modifier = Modifier.testTag(ProfileTestTags.SAVE_BUTTON),
    )

    TextButton(
        onClick = viewModel::onCancelEditing,
        enabled = !state.isSaving,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .testTag(ProfileTestTags.CANCEL_BUTTON),
    ) {
        Text(stringResource(R.string.profile_cancel_edit), color = PurplePrimary)
    }
}

@Composable
private fun ProfileViewCard(state: ProfileUiState) {
    val notSet = stringResource(R.string.profile_not_set)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProfileInfoRow(stringResource(R.string.profile_name_label), state.name.ifBlank { notSet })
            ProfileInfoRow(stringResource(R.string.profile_last_name_label), state.lastName.ifBlank { notSet })
            ProfileInfoRow(stringResource(R.string.profile_email_label), state.email.ifBlank { notSet })
            ProfileInfoRow(stringResource(R.string.profile_username_label), state.userName.ifBlank { notSet })
            ProfileInfoRow(
                stringResource(R.string.profile_birthdate_label),
                state.birthdate.ifBlank { notSet },
            )
            ProfileInfoRow(
                stringResource(R.string.profile_bio_label),
                state.bio.ifBlank { notSet },
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = DarkOnSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = DarkOnBackground,
        )
    }
}

@Composable
private fun AvatarSection(
    avatarUri: String?,
    avatarError: FieldError?,
    serverError: String?,
    editable: Boolean,
    onPickAvatar: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(DarkSurfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (avatarUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatarUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.cd_profile_avatar),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = stringResource(R.string.cd_profile_avatar),
                    tint = DarkOnBackground,
                    modifier = Modifier.size(48.dp),
                )
            }
        }
        if (editable) {
            OutlinedButton(
                onClick = onPickAvatar,
                modifier = Modifier.testTag(ProfileTestTags.AVATAR_BUTTON),
            ) {
                Text(stringResource(R.string.profile_avatar_change))
            }
            Text(
                text = stringResource(R.string.profile_avatar_hint),
                style = MaterialTheme.typography.bodySmall,
                color = DarkOnSurfaceVariant,
            )
            avatarError?.let {
                Text(
                    text = when (it) {
                        is FieldError.TooLong -> stringResource(R.string.profile_avatar_too_large)
                        else -> stringResource(R.string.profile_avatar_invalid_type)
                    },
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            serverError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: FieldError?,
    serverError: String?,
    placeholder: String = "",
    minLines: Int = 1,
    maxLength: Int? = null,
    testTag: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PurplePrimary,
        unfocusedBorderColor = DarkSurfaceVariant,
        focusedTextColor = DarkOnBackground,
        unfocusedTextColor = DarkOnBackground,
        cursorColor = PurplePrimary,
    )
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder.isNotBlank()) {
            { Text(placeholder) }
        } else {
            null
        },
        isError = error != null || serverError != null,
        supportingText = {
            when {
                serverError != null -> Text(serverError)
                error != null -> Text(error.toMessage())
                maxLength != null -> Text("${value.length}/$maxLength")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        minLines = minLines,
        maxLines = if (minLines > 1) 5 else 1,
        singleLine = minLines == 1,
        shape = RoundedCornerShape(14.dp),
        colors = fieldColors,
        keyboardOptions = keyboardOptions,
    )
}

@Composable
private fun mapBannerMessage(key: String): String = when (key) {
    "network" -> stringResource(R.string.profile_error_network)
    "conflict" -> stringResource(R.string.profile_error_conflict)
    "precondition_failed" -> stringResource(R.string.profile_error_precondition)
    else -> key.ifBlank { stringResource(R.string.login_error_unknown) }
}
