package com.example.leveluplife.data.player

sealed class ProfileField {
    data object Name : ProfileField()
    data object LastName : ProfileField()
    data object Email : ProfileField()
    data object Birthdate : ProfileField()
    data object UserName : ProfileField()
    data object Avatar : ProfileField()
    data object Bio : ProfileField()
    data object General : ProfileField()
}

sealed class ProfileError(open val message: String) {
    data class Network(override val message: String) : ProfileError(message)
    data class Unauthorized(override val message: String) : ProfileError(message)
    data class NotFound(override val message: String) : ProfileError(message)
    data class Conflict(override val message: String) : ProfileError(message)
    data class PreconditionFailed(override val message: String) : ProfileError(message)
    data class RateLimited(override val message: String) : ProfileError(message)
    data class Validation(
        override val message: String,
        val fieldErrors: Map<ProfileField, String> = emptyMap(),
    ) : ProfileError(message)
    data class Server(override val message: String) : ProfileError(message)
    data class Unknown(override val message: String) : ProfileError(message)
}

class ProfileException(val error: ProfileError) : Exception(error.message)
