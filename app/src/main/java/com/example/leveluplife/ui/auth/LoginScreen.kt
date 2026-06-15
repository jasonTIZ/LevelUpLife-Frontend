package com.example.leveluplife.ui.auth

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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leveluplife.R
import com.example.leveluplife.data.error.AuthError
import com.example.leveluplife.ui.components.LulErrorAlertDialog
import com.example.leveluplife.ui.components.LulLogo
import com.example.leveluplife.ui.components.LulPrimaryButton
import com.example.leveluplife.ui.components.LulScreenHeaderLabels
import com.example.leveluplife.ui.components.ThemeToggleButton
import com.example.leveluplife.ui.theme.ThemeController

object LoginTestTags {
    const val EMAIL_FIELD = "login_email_field"
    const val PASSWORD_FIELD = "login_password_field"
    const val SUBMIT_BUTTON = "login_submit_button"
    const val LOADING = "login_loading"
    const val ERROR_BANNER = "login_error_banner"
    const val THEME_TOGGLE = "login_theme_toggle"
    const val RETRY_BUTTON = "login_retry_button"
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    themeController: ThemeController,
    onLoggedIn: () -> Unit,
    onNavigateToRegister: () -> Unit,
    infoMessage: String? = null,
    onInfoMessageShown: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    LaunchedEffect(state.loggedInUser) {
        if (state.loggedInUser != null) {
            onLoggedIn()
            viewModel.consumeLoggedIn()
        }
    }

    LaunchedEffect(infoMessage) {
        if (!infoMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(infoMessage)
            onInfoMessageShown()
        }
    }

    androidx.compose.material3.Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { innerPadding ->
        LoginContent(
            state = state,
            themeController = themeController,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
            onSubmit = viewModel::onSubmit,
            onDismissError = viewModel::dismissError,
            onNavigateToRegister = onNavigateToRegister,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun LoginContent(
    state: LoginUiState,
    themeController: ThemeController,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onDismissError: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) { runCatching { emailFocus.requestFocus() } }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LulLogo()
                ThemeToggleButton(
                    controller = themeController,
                    modifier = Modifier.testTag(LoginTestTags.THEME_TOGGLE),
                )
            }

            Spacer(Modifier.height(48.dp))

            LulScreenHeaderLabels(
                title = stringResource(R.string.login_welcome),
                subtitle = stringResource(R.string.login_welcome_subtitle),
                titleTextStyle = MaterialTheme.typography.headlineMedium,
                titleFontWeight = FontWeight.Bold,
                titleColor = MaterialTheme.colorScheme.onBackground,
                subtitleTextStyle = MaterialTheme.typography.bodyLarge,
                subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
                gapAfterTitle = 4.dp,
            )

            Spacer(Modifier.height(28.dp))
            // Email o usuario
            val emailFieldLabel = stringResource(R.string.login_email_placeholder)
            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                singleLine = true,
                isError = state.emailError != null,
                shape = RoundedCornerShape(14.dp),
                colors = filledFieldColors(),
                placeholder = { Text(emailFieldLabel) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                supportingText = {
                    state.emailError?.let { Text(text = it.toMessage()) }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailFocus)
                    .testTag(LoginTestTags.EMAIL_FIELD)
                    .semantics {
                        contentDescription = emailFieldLabel
                    },
            )

            Spacer(Modifier.height(12.dp))

            // Password
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                singleLine = true,
                isError = state.passwordError != null,
                shape = RoundedCornerShape(14.dp),
                colors = filledFieldColors(),
                placeholder = { Text(stringResource(R.string.login_password_placeholder)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingIcon = {
                    val cdRes = if (state.passwordVisible)
                        R.string.cd_hide_password else R.string.cd_show_password
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (state.passwordVisible)
                                Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = stringResource(cdRes),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                visualTransformation = if (state.passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                supportingText = {
                    state.passwordError?.let { Text(text = it.toMessage()) }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    onSubmit()
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocus)
                    .testTag(LoginTestTags.PASSWORD_FIELD)
                    .semantics {
                        contentDescription = "Contraseña"
                    },
            )

            Spacer(Modifier.height(20.dp))

            LulPrimaryButton(
                text = stringResource(R.string.login_submit),
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
                modifier = Modifier.testTag(LoginTestTags.SUBMIT_BUTTON),
                loadingTestTag = LoginTestTags.LOADING,
            )

            Spacer(Modifier.height(40.dp))
        }

        // Footer "¿NO TIENES CUENTA? REGÍSTRATE"
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.login_no_account),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.width(6.dp))
            TextButton(
                onClick = onNavigateToRegister,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
            ) {
                Text(
                    text = stringResource(R.string.login_register_cta),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                )
            }
        }

        state.bannerError?.let { error ->
            LulErrorAlertDialog(
                title = stringResource(R.string.error_dialog_title),
                message = error.toMessage(),
                dismissText = stringResource(R.string.login_dismiss),
                onDismiss = onDismissError,
                titleTextStyle = MaterialTheme.typography.titleLarge,
                messageTextStyle = MaterialTheme.typography.bodyMedium,
                iconTint = MaterialTheme.colorScheme.error,
                retryText = if (error is AuthError.Network) {
                    stringResource(R.string.login_retry)
                } else {
                    null
                },
                onRetry = if (error is AuthError.Network) onSubmit else null,
                retryTestTag = LoginTestTags.RETRY_BUTTON,
                errorTestTag = LoginTestTags.ERROR_BANNER,
            )
        }
    }
}

@Composable
private fun filledFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    errorContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledBorderColor = MaterialTheme.colorScheme.surfaceVariant,
    errorBorderColor = MaterialTheme.colorScheme.error,
)
