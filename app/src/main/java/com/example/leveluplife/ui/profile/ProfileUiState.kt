package com.example.leveluplife.ui.profile

import com.example.leveluplife.data.player.ProfileField
import com.example.leveluplife.domain.validation.AvatarValidationError
import com.example.leveluplife.domain.validation.FieldError

data class ProfileFormSnapshot(
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    val userName: String = "",
    val birthdate: String = "",
    val bio: String = "",
    val avatarUri: String? = null,
) {
    companion object {
        fun from(state: ProfileUiState): ProfileFormSnapshot = ProfileFormSnapshot(
            name = state.name,
            lastName = state.lastName,
            email = state.email,
            userName = state.userName,
            birthdate = state.birthdate,
            bio = state.bio,
            avatarUri = state.avatarUri,
        )
    }
}

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    val userName: String = "",
    val birthdate: String = "",
    val bio: String = "",
    val className: String = "",
    val level: Int = 1,
    val avatarUri: String? = null,
    val pendingAvatarUri: String? = null,
    val pendingAvatarMimeType: String? = null,
    val editSnapshot: ProfileFormSnapshot? = null,
    val etag: String = "",
    val nameError: FieldError? = null,
    val lastNameError: FieldError? = null,
    val emailError: FieldError? = null,
    val userNameError: FieldError? = null,
    val birthdateError: FieldError? = null,
    val bioError: FieldError? = null,
    val avatarError: AvatarValidationError? = null,
    val serverFieldErrors: Map<ProfileFormField, String> = emptyMap(),
    val bannerError: String? = null,
    val profileSaved: Boolean = false,
    val successMessage: String? = null,
)

enum class ProfileFormField {
    NAME,
    LAST_NAME,
    EMAIL,
    USER_NAME,
    BIRTHDATE,
    BIO,
    AVATAR,
}

fun ProfileField.toFormField(): ProfileFormField? = when (this) {
    ProfileField.Name -> ProfileFormField.NAME
    ProfileField.LastName -> ProfileFormField.LAST_NAME
    ProfileField.Email -> ProfileFormField.EMAIL
    ProfileField.UserName -> ProfileFormField.USER_NAME
    ProfileField.Birthdate -> ProfileFormField.BIRTHDATE
    ProfileField.Bio -> ProfileFormField.BIO
    ProfileField.Avatar -> ProfileFormField.AVATAR
    ProfileField.General -> null
}
