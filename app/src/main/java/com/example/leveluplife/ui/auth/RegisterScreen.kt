package com.example.leveluplife.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leveluplife.R
import com.example.leveluplife.data.error.AuthError
import com.example.leveluplife.domain.validation.FieldError
import com.example.leveluplife.domain.validation.RegisterFieldKey
import com.example.leveluplife.ui.components.LulErrorAlertDialog
import com.example.leveluplife.ui.components.LulLogo
import com.example.leveluplife.ui.components.LulPrimaryButton
import com.example.leveluplife.ui.components.LulScreenHeaderLabels
import com.example.leveluplife.ui.components.PasswordStrengthIndicator
import com.example.leveluplife.ui.theme.ThemeController
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object RegisterTestTags {
    const val NAME_FIELD = "register_name_field"
    const val NEXT_BUTTON = "register_next_button"
    const val SUBMIT_BUTTON = "register_submit_button"
}

private val DefaultBirthdateMillis: Long =
    LocalDate.of(2000, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    themeController: ThemeController,
    onRegisteredAndLoggedIn: () -> Unit,
    onNavigateToLogin: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()

    LaunchedEffect(state.loggedInUser, systemDark) {
        if (state.loggedInUser != null) {
            themeController.lockCurrentAppearance(systemDark)
            onRegisteredAndLoggedIn()
            viewModel.consumeLoggedIn()
        }
    }

    LaunchedEffect(state.navigateToLoginMessage) {
        val message = state.navigateToLoginMessage
        if (message != null) {
            onNavigateToLogin(message)
            viewModel.consumeNavigateToLogin()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { innerPadding ->
        RegisterContent(
            state = state,
            onNameChange = viewModel::onNameChange,
            onLastNameChange = viewModel::onLastNameChange,
            onEmailChange = viewModel::onEmailChange,
            onBirthdateChange = viewModel::onBirthdateChange,
            onUserNameChange = viewModel::onUserNameChange,
            onPasswordChange = viewModel::onPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
            onToggleConfirmPasswordVisibility = viewModel::onToggleConfirmPasswordVisibility,
            onClassSelected = viewModel::onClassSelected,
            onNextStep = viewModel::onNextStep,
            onPreviousStep = viewModel::onPreviousStep,
            onSubmit = viewModel::onSubmit,
            onDismissError = viewModel::dismissError,
            onNavigateToLogin = { onNavigateToLogin(null) },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterContent(
    state: RegisterUiState,
    onNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onBirthdateChange: (String) -> Unit,
    onUserNameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onClassSelected: (Int) -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onSubmit: () -> Unit,
    onDismissError: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var showDatePicker by remember { mutableStateOf(false) }
    var classMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            LulLogo()

            Spacer(Modifier.height(32.dp))

            if (state.step == RegisterStep.PERSONAL) {
                LulScreenHeaderLabels(
                    title = stringResource(R.string.register_title),
                    subtitle = stringResource(R.string.register_subtitle),
                    titleTextStyle = MaterialTheme.typography.headlineMedium,
                    titleFontWeight = FontWeight.Bold,
                    titleColor = MaterialTheme.colorScheme.onBackground,
                    subtitleTextStyle = MaterialTheme.typography.bodyLarge,
                    subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    gapAfterTitle = 4.dp,
                )

                Spacer(Modifier.height(24.dp))

                RegisterOutlinedField(
                    value = state.name,
                    onValueChange = onNameChange,
                    placeholder = stringResource(R.string.register_name_placeholder),
                    isError = state.nameError != null || state.serverFieldErrors.containsKey(RegisterFieldKey.NAME),
                    errorMessage = fieldMessage(state.nameError, state.serverFieldErrors[RegisterFieldKey.NAME]),
                    leadingIcon = {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.testTag(RegisterTestTags.NAME_FIELD),
                )

                Spacer(Modifier.height(12.dp))

                RegisterOutlinedField(
                    value = state.lastName,
                    onValueChange = onLastNameChange,
                    placeholder = stringResource(R.string.register_last_name_placeholder),
                    isError = state.lastNameError != null || state.serverFieldErrors.containsKey(RegisterFieldKey.LAST_NAME),
                    errorMessage = fieldMessage(state.lastNameError, state.serverFieldErrors[RegisterFieldKey.LAST_NAME]),
                    leadingIcon = {
                        Icon(Icons.Outlined.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                )

                Spacer(Modifier.height(12.dp))

                BirthdateField(
                    value = state.birthdate,
                    placeholder = stringResource(R.string.register_birthdate_placeholder),
                    isError = state.birthdateError != null || state.serverFieldErrors.containsKey(RegisterFieldKey.BIRTHDATE),
                    errorMessage = fieldMessage(state.birthdateError, state.serverFieldErrors[RegisterFieldKey.BIRTHDATE]),
                    onOpenPicker = { showDatePicker = true },
                )

                Spacer(Modifier.height(20.dp))

                LulPrimaryButton(
                    text = stringResource(R.string.register_next),
                    onClick = {
                        focusManager.clearFocus()
                        onNextStep()
                    },
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    ),
                    buttonHeight = 56.dp,
                    modifier = Modifier.testTag(RegisterTestTags.NEXT_BUTTON),
                )
            } else {
                TextButton(
                    onClick = onPreviousStep,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.align(Alignment.Start),
                ) {
                    Text(
                        text = stringResource(R.string.register_back),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                    )
                }

                Spacer(Modifier.height(8.dp))

                LulScreenHeaderLabels(
                    title = stringResource(R.string.register_step2_title),
                    subtitle = stringResource(R.string.register_step2_subtitle),
                    titleTextStyle = MaterialTheme.typography.headlineMedium,
                    titleFontWeight = FontWeight.Bold,
                    titleColor = MaterialTheme.colorScheme.onBackground,
                    subtitleTextStyle = MaterialTheme.typography.bodyLarge,
                    subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    gapAfterTitle = 4.dp,
                )

                Spacer(Modifier.height(24.dp))

                RegisterOutlinedField(
                    value = state.userName,
                    onValueChange = onUserNameChange,
                    placeholder = stringResource(R.string.register_username_placeholder),
                    isError = state.userNameError != null || state.serverFieldErrors.containsKey(RegisterFieldKey.USER_NAME),
                    errorMessage = fieldMessage(state.userNameError, state.serverFieldErrors[RegisterFieldKey.USER_NAME]),
                    leadingIcon = {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                )

                Spacer(Modifier.height(12.dp))

                RegisterOutlinedField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    placeholder = stringResource(R.string.register_email_placeholder),
                    isError = state.emailError != null || state.serverFieldErrors.containsKey(RegisterFieldKey.EMAIL),
                    errorMessage = fieldMessage(state.emailError, state.serverFieldErrors[RegisterFieldKey.EMAIL]),
                    leadingIcon = {
                        Icon(Icons.Outlined.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                )

                Spacer(Modifier.height(12.dp))

                RegisterOutlinedField(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    placeholder = stringResource(R.string.register_password_placeholder),
                    isError = state.passwordError != null || state.serverFieldErrors.containsKey(RegisterFieldKey.PASSWORD),
                    errorMessage = fieldMessage(state.passwordError, state.serverFieldErrors[RegisterFieldKey.PASSWORD]),
                    leadingIcon = {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        PasswordVisibilityToggle(visible = state.passwordVisible, onToggle = onTogglePasswordVisibility)
                    },
                    visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                )

                if (state.password.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    PasswordStrengthIndicator(password = state.password)
                }

                Spacer(Modifier.height(12.dp))

                RegisterOutlinedField(
                    value = state.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    placeholder = stringResource(R.string.register_confirm_password_placeholder),
                    isError = state.confirmPasswordError != null,
                    errorMessage = fieldMessage(state.confirmPasswordError, null),
                    leadingIcon = {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        PasswordVisibilityToggle(
                            visible = state.confirmPasswordVisible,
                            onToggle = onToggleConfirmPasswordVisibility,
                        )
                    },
                    visualTransformation = if (state.confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                )

                Spacer(Modifier.height(12.dp))

                ClassSelector(
                    selectedClassId = state.classId,
                    expanded = classMenuExpanded,
                    onExpandedChange = { classMenuExpanded = it },
                    onClassSelected = {
                        onClassSelected(it)
                        classMenuExpanded = false
                    },
                    isError = state.classIdError != null || state.serverFieldErrors.containsKey(RegisterFieldKey.CLASS_ID),
                    errorMessage = fieldMessage(state.classIdError, state.serverFieldErrors[RegisterFieldKey.CLASS_ID]),
                )

                Spacer(Modifier.height(20.dp))

                LulPrimaryButton(
                    text = stringResource(R.string.register_submit),
                    onClick = {
                        focusManager.clearFocus()
                        onSubmit()
                    },
                    enabled = !state.isLoading,
                    isLoading = state.isLoading,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    ),
                    buttonHeight = 56.dp,
                    modifier = Modifier.testTag(RegisterTestTags.SUBMIT_BUTTON),
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.register_has_account),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.width(6.dp))
                TextButton(
                    onClick = onNavigateToLogin,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                ) {
                    Text(
                        text = stringResource(R.string.register_login_cta),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        state.bannerError?.let { error ->
            LulErrorAlertDialog(
                title = stringResource(R.string.error_dialog_title),
                message = error.toRegisterMessage(),
                dismissText = stringResource(R.string.login_dismiss),
                onDismiss = onDismissError,
                titleTextStyle = MaterialTheme.typography.titleLarge,
                messageTextStyle = MaterialTheme.typography.bodyMedium,
                iconTint = MaterialTheme.colorScheme.error,
                retryText = if (error is AuthError.Network) stringResource(R.string.login_retry) else null,
                onRetry = if (error is AuthError.Network) onSubmit else null,
            )
        }
    }

    if (showDatePicker) {
        BirthdatePickerDialog(
            currentValue = state.birthdate,
            onDismiss = { showDatePicker = false },
            onConfirm = { selected ->
                onBirthdateChange(selected)
                showDatePicker = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthdatePickerDialog(
    currentValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val todayUtc = remember { LocalDate.now(ZoneOffset.UTC) }
    val initialMillis = parseBirthdateToMillis(currentValue) ?: DefaultBirthdateMillis
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                return !date.isAfter(todayUtc)
            }

            override fun isSelectableYear(year: Int): Boolean =
                year in 1900..todayUtc.year
        },
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onConfirm(formatBirthdate(it)) }
                },
            ) {
                Text(stringResource(R.string.register_date_picker_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_cancel))
            }
        },
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = false,
        )
    }
}

@Composable
private fun BirthdateField(
    value: String,
    placeholder: String,
    isError: Boolean,
    errorMessage: String?,
    onOpenPicker: () -> Unit,
) {
    val displayText = value.ifBlank { "" }
    OutlinedTextField(
        value = displayText,
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        isError = isError,
        shape = RoundedCornerShape(14.dp),
        colors = filledFieldColors(),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingIcon = {
            IconButton(onClick = onOpenPicker) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = stringResource(R.string.cd_open_date_picker),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        supportingText = errorMessage?.let { { Text(text = it) } },
        modifier = Modifier
            .fillMaxWidth()
            .clickableWithoutRipple(onClick = onOpenPicker),
    )
}

@Composable
private fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.then(
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        ),
    )
}

/** Mismo `OutlinedTextField` del login, con placeholder/ícono/error parametrizables. */
@Composable
private fun RegisterOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        readOnly = readOnly,
        isError = isError,
        shape = RoundedCornerShape(14.dp),
        colors = filledFieldColors(),
        placeholder = { Text(placeholder) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = errorMessage?.let { { Text(text = it) } },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClassSelector(
    selectedClassId: Int?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onClassSelected: (Int) -> Unit,
    isError: Boolean,
    errorMessage: String?,
) {
    val selectedLabel = CharacterClasses.available
        .firstOrNull { it.id == selectedClassId }
        ?.let { stringResource(it.labelRes) }
        ?: stringResource(R.string.register_class_placeholder)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            isError = isError,
            shape = RoundedCornerShape(14.dp),
            colors = filledFieldColors(),
            placeholder = { Text(stringResource(R.string.register_class_placeholder)) },
            leadingIcon = {
                Icon(Icons.Outlined.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            supportingText = errorMessage?.let { { Text(text = it) } },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            CharacterClasses.available.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes)) },
                    onClick = { onClassSelected(option.id) },
                )
            }
        }
    }
}

@Composable
private fun PasswordVisibilityToggle(
    visible: Boolean,
    onToggle: () -> Unit,
) {
    val cdRes = if (visible) R.string.cd_hide_password else R.string.cd_show_password
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
            contentDescription = stringResource(cdRes),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun fieldMessage(fieldError: FieldError?, serverMessage: String?): String? {
    serverMessage?.takeIf { it.isNotBlank() }?.let { return it }
    return fieldError?.toMessage()
}

private fun parseBirthdateToMillis(value: String): Long? = runCatching {
    LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
}.getOrNull()

private fun formatBirthdate(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .format(DateTimeFormatter.ISO_LOCAL_DATE)
