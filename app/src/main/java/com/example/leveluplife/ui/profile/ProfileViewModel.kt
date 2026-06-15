package com.example.leveluplife.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leveluplife.data.player.PlayerProfile
import com.example.leveluplife.data.player.ProfileAvatarStorage
import com.example.leveluplife.data.player.ProfileCache
import com.example.leveluplife.data.player.ProfileError
import com.example.leveluplife.data.player.ProfileException
import com.example.leveluplife.data.player.ProfileRepository
import com.example.leveluplife.domain.validation.AvatarValidationError
import com.example.leveluplife.domain.validation.AvatarValidator
import com.example.leveluplife.domain.validation.FieldError
import com.example.leveluplife.domain.validation.ProfileFieldKey
import com.example.leveluplife.domain.validation.ProfileFormValues
import com.example.leveluplife.domain.validation.ProfileInputSanitizer
import com.example.leveluplife.domain.validation.ProfileValidators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val profileCache: ProfileCache,
    private val avatarStorage: ProfileAvatarStorage,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            profileCache.loadPersisted()
            loadProfile()
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, bannerError = null) }
            profileRepository.fetchProfile()
                .onSuccess { result ->
                    applyProfile(result.profile, result.etag)
                    _state.update { it.copy(isLoading = false) }
                }
                .onFailure { throwable ->
                    if ((throwable as? ProfileException)?.error is ProfileError.Unauthorized) {
                        _state.update { it.copy(isLoading = false) }
                        return@launch
                    }
                    val cached = profileCache.profile.value
                    if (cached != null) {
                        val etag = profileCache.currentEtag().orEmpty()
                        applyProfile(cached, etag)
                        _state.update { it.copy(isLoading = false) }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                bannerError = mapThrowableMessage(throwable),
                            )
                        }
                    }
                }
        }
    }

    fun onNameChange(value: String) = onFieldChange(
        rawValue = value,
        sanitize = ProfileInputSanitizer::sanitizePersonName,
        validate = ProfileValidators::validateName,
        field = ProfileFormField.NAME,
    ) { current, sanitized, error ->
        current.copy(name = sanitized, nameError = error)
    }

    fun onLastNameChange(value: String) = onFieldChange(
        rawValue = value,
        sanitize = ProfileInputSanitizer::sanitizePersonName,
        validate = ProfileValidators::validateLastName,
        field = ProfileFormField.LAST_NAME,
    ) { current, sanitized, error ->
        current.copy(lastName = sanitized, lastNameError = error)
    }

    fun onEmailChange(value: String) = onFieldChange(
        rawValue = value,
        sanitize = ProfileInputSanitizer::sanitizeEmail,
        validate = ProfileValidators::validateEmail,
        field = ProfileFormField.EMAIL,
    ) { current, sanitized, error ->
        current.copy(email = sanitized, emailError = error)
    }

    fun onUserNameChange(value: String) = onFieldChange(
        rawValue = value,
        sanitize = ProfileInputSanitizer::sanitizeUserName,
        validate = ProfileValidators::validateUserName,
        field = ProfileFormField.USER_NAME,
    ) { current, sanitized, error ->
        current.copy(userName = sanitized, userNameError = error)
    }

    fun onBirthdateChange(value: String) = onFieldChange(
        rawValue = value,
        sanitize = ProfileInputSanitizer::sanitizeBirthdate,
        validate = ProfileValidators::validateBirthdate,
        field = ProfileFormField.BIRTHDATE,
    ) { current, sanitized, error ->
        current.copy(birthdate = sanitized, birthdateError = error)
    }

    fun onBioChange(value: String) = onFieldChange(
        rawValue = value,
        sanitize = ProfileInputSanitizer::sanitizeBio,
        validate = ProfileValidators::validateBio,
        field = ProfileFormField.BIO,
    ) { current, sanitized, error ->
        current.copy(bio = sanitized, bioError = error)
    }

    fun onAvatarSelected(uri: String, mimeType: String?, sizeBytes: Long) {
        when (AvatarValidator.validate(mimeType, sizeBytes)) {
            null -> {
                viewModelScope.launch {
                    val persistedUri = avatarStorage.persistFromPickerUri(uri, mimeType)
                    if (persistedUri == null) {
                        _state.update { it.copy(avatarError = AvatarValidationError.PersistFailed) }
                        return@launch
                    }
                    _state.update {
                        it.copy(
                            pendingAvatarUri = persistedUri,
                            avatarUri = persistedUri,
                            avatarError = null,
                            serverFieldErrors = it.serverFieldErrors - ProfileFormField.AVATAR,
                        )
                    }
                    profileCache.updateLocalExtras(persistedUri, _state.value.bio)
                }
            }
            AvatarValidationError.TooLarge -> {
                _state.update { it.copy(avatarError = AvatarValidationError.TooLarge) }
            }
            AvatarValidationError.UnsupportedType -> {
                _state.update { it.copy(avatarError = AvatarValidationError.UnsupportedType) }
            }
        }
    }

    fun onStartEditing() {
        val sanitized = sanitizeState(_state.value)
        _state.update {
            sanitized.copy(
                isEditing = true,
                editSnapshot = ProfileFormSnapshot.from(sanitized),
                nameError = null,
                lastNameError = null,
                emailError = null,
                userNameError = null,
                birthdateError = null,
                bioError = null,
                avatarError = null,
                serverFieldErrors = emptyMap(),
                bannerError = null,
                pendingAvatarUri = null,
            )
        }
    }

    fun onCancelEditing() {
        val snapshot = _state.value.editSnapshot ?: run {
            _state.update { it.copy(isEditing = false) }
            return
        }
        _state.update {
            it.copy(
                isEditing = false,
                editSnapshot = null,
                name = snapshot.name,
                lastName = snapshot.lastName,
                email = snapshot.email,
                userName = snapshot.userName,
                birthdate = snapshot.birthdate,
                bio = snapshot.bio,
                avatarUri = snapshot.avatarUri,
                pendingAvatarUri = null,
                nameError = null,
                lastNameError = null,
                emailError = null,
                userNameError = null,
                birthdateError = null,
                bioError = null,
                avatarError = null,
                serverFieldErrors = emptyMap(),
                bannerError = null,
            )
        }
    }

    fun onSubmit() {
        val validated = validateAll()
        _state.value = validated
        if (hasValidationErrors(validated)) return

        viewModelScope.launch {
            val etag = validated.etag.ifBlank { profileCache.currentEtag().orEmpty() }
            if (etag.isBlank()) {
                _state.update { it.copy(bannerError = "missing_etag") }
                return@launch
            }

            _state.update { it.copy(isSaving = true, bannerError = null, profileSaved = false) }

            profileCache.updateLocalExtras(
                avatarUri = validated.pendingAvatarUri ?: validated.avatarUri,
                bio = validated.bio,
            )

            profileRepository.updateProfile(
                etag = etag,
                name = validated.name,
                lastName = validated.lastName,
                email = validated.email,
                birthdate = validated.birthdate.ifBlank { null },
                userName = validated.userName,
            ).onSuccess { profile ->
                applyProfile(
                    profile.copy(
                        avatarUri = validated.pendingAvatarUri ?: validated.avatarUri,
                        bio = validated.bio,
                    ),
                    profileCache.currentEtag().orEmpty(),
                )
                _state.update {
                    it.copy(
                        isSaving = false,
                        isEditing = false,
                        editSnapshot = null,
                        profileSaved = true,
                        successMessage = "saved",
                        pendingAvatarUri = null,
                    )
                }
            }.onFailure { throwable ->
                handleSubmitFailure(throwable)
            }
        }
    }

    fun consumeSavedEvent() {
        _state.update { it.copy(profileSaved = false, successMessage = null) }
    }

    fun dismissError() {
        _state.update { it.copy(bannerError = null) }
    }

    private fun handleSubmitFailure(throwable: Throwable) {
        when (val error = (throwable as? ProfileException)?.error) {
            is ProfileError.Unauthorized -> {
                _state.update { it.copy(isSaving = false) }
            }
            is ProfileError.Validation -> {
                val serverErrors = error.fieldErrors.mapNotNull { (field, message) ->
                    field.toFormField()?.let { it to message }
                }.toMap()
                _state.update {
                    it.copy(
                        isSaving = false,
                        bannerError = error.message,
                        serverFieldErrors = serverErrors,
                    )
                }
            }
            is ProfileError.PreconditionFailed -> {
                _state.update { it.copy(isSaving = false, bannerError = "precondition_failed") }
                loadProfile()
            }
            else -> {
                _state.update {
                    it.copy(
                        isSaving = false,
                        bannerError = mapThrowableMessage(throwable),
                    )
                }
            }
        }
    }

    private fun validateAll(): ProfileUiState {
        val sanitized = sanitizeState(_state.value)
        val errors = ProfileValidators.validateAll(sanitized.toFormValues())
        return sanitized.copy(
            nameError = errors[ProfileFieldKey.NAME],
            lastNameError = errors[ProfileFieldKey.LAST_NAME],
            emailError = errors[ProfileFieldKey.EMAIL],
            userNameError = errors[ProfileFieldKey.USER_NAME],
            birthdateError = errors[ProfileFieldKey.BIRTHDATE],
            bioError = errors[ProfileFieldKey.BIO],
            avatarError = _state.value.avatarError,
        )
    }

    private fun sanitizeState(state: ProfileUiState): ProfileUiState {
        val sanitized = ProfileInputSanitizer.sanitizeProfile(state.toFormValues())
        return state.copy(
            name = sanitized.name,
            lastName = sanitized.lastName,
            email = sanitized.email,
            userName = sanitized.userName,
            birthdate = sanitized.birthdate,
            bio = sanitized.bio,
        )
    }

    private fun ProfileUiState.toFormValues(): ProfileFormValues = ProfileFormValues(
        name = name,
        lastName = lastName,
        email = email,
        userName = userName,
        birthdate = birthdate,
        bio = bio,
    )

    private fun onFieldChange(
        rawValue: String,
        sanitize: (String) -> String,
        validate: (String) -> FieldError?,
        field: ProfileFormField,
        reducer: (ProfileUiState, String, FieldError?) -> ProfileUiState,
    ) {
        val sanitized = sanitize(rawValue)
        _state.update { current ->
            val error = if (current.isEditing) validate(sanitized) else null
            reducer(current, sanitized, error)
                .copy(serverFieldErrors = current.serverFieldErrors - field)
        }
    }

    private fun hasValidationErrors(state: ProfileUiState): Boolean =
        listOf(
            state.nameError,
            state.lastNameError,
            state.emailError,
            state.userNameError,
            state.birthdateError,
            state.bioError,
            state.avatarError,
        ).any { it != null }

    private fun applyProfile(profile: PlayerProfile, etag: String) {
        _state.update {
            it.copy(
                name = profile.name,
                lastName = profile.lastName,
                email = profile.email,
                userName = profile.userName,
                birthdate = profile.birthdate.orEmpty(),
                bio = profile.bio,
                className = profile.className,
                level = profile.level,
                avatarUri = profile.avatarUri,
                pendingAvatarUri = null,
                etag = etag,
                isEditing = false,
                editSnapshot = null,
            )
        }
    }

    private fun mapThrowableMessage(throwable: Throwable): String =
        when (val error = (throwable as? ProfileException)?.error) {
            is ProfileError.Network -> "network"
            is ProfileError.Conflict -> "conflict"
            is ProfileError.PreconditionFailed -> "precondition_failed"
            is ProfileError.Validation -> error.message
            is ProfileError -> error.message
            else -> throwable.message ?: "unknown"
        }

    class Factory(
        private val profileRepository: ProfileRepository,
        private val profileCache: ProfileCache,
        private val avatarStorage: ProfileAvatarStorage,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel(profileRepository, profileCache, avatarStorage) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
