package com.example.leveluplife.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.leveluplife.R
import com.example.leveluplife.data.player.ProfileError
import com.example.leveluplife.domain.validation.AvatarValidationError

@Composable
fun AvatarValidationError.toMessage(): String = when (this) {
    AvatarValidationError.TooLarge -> stringResource(R.string.profile_avatar_too_large)
    AvatarValidationError.UnsupportedType -> stringResource(R.string.profile_avatar_invalid_type)
}

@Composable
fun ProfileError.toMessage(): String = when (this) {
    is ProfileError.Network -> stringResource(R.string.profile_error_network)
    is ProfileError.Unauthorized -> stringResource(R.string.login_error_session_expired)
    is ProfileError.NotFound -> stringResource(R.string.profile_load_error)
    is ProfileError.Conflict -> stringResource(R.string.profile_error_conflict)
    is ProfileError.PreconditionFailed -> stringResource(R.string.profile_error_precondition)
    is ProfileError.Validation -> message.ifBlank { stringResource(R.string.login_error_bad_request) }
    is ProfileError.Server -> stringResource(R.string.login_error_server)
    is ProfileError.Unknown -> stringResource(R.string.login_error_unknown)
}
