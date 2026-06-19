package com.example.leveluplife.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPlayerProfileResponseDto(
    @SerialName("PlayerUserId") val playerUserId: String = "",
    @SerialName("PlayerUserUserName") val playerUserUserName: String = "",
    @SerialName("PlayerUserLevel") val playerUserLevel: Int = 0,
    val totalExperiencePoints: Int = 0,
    val experiencePointsInCurrentLevel: Int = 0,
    val experiencePointsRequiredForNextLevel: Int = 0,
    val levelProgressPercent: Double = 0.0,
    val daysStreak: Int = 0,
    @SerialName("statusIsActive") val statusIsActive: Boolean = true,
    @SerialName("PlayerUserLastLogin") val playerUserLastLogin: String? = null,
    @SerialName("bio") val bio: String? = null,
    @SerialName("avatarUrl") val avatarUrl: String? = null,
    @SerialName("PersonData") val personData: GetPlayerProfilePersonDataDto = GetPlayerProfilePersonDataDto(),
)

@Serializable
data class GetPlayerProfilePersonDataDto(
    @SerialName("name") val name: String = "",
    @SerialName("lastName") val lastName: String = "",
    @SerialName("email") val email: String = "",
    @SerialName("birthdate") val birthdate: String? = null,
)

@Serializable
data class UpdatePlayerProfileRequestDto(
    val personData: PersonUpdateRequestDto? = null,
    val playerData: PlayerDataUpdateRequestDto? = null,
)

@Serializable
data class PersonUpdateRequestDto(
    val name: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val birthdate: String? = null,
)

@Serializable
data class PlayerDataUpdateRequestDto(
    val userName: String? = null,
    val preferredClassId: Int? = null,
    val bio: String? = null,
    val timezone: String? = null,
)

@Serializable
data class UpdatePlayerProfileResponseDto(
    val success: Boolean = false,
    val message: String = "",
    val player: PlayerProfileDto = PlayerProfileDto(),
)

@Serializable
data class PlayerProfileDto(
    val id: Int = 0,
    val userName: String = "",
    val level: Int = 0,
    val isActive: Boolean = true,
    val lastLogin: String? = null,
    val classId: Int = 0,
    val className: String = "",
    val bio: String? = null,
    val avatarUrl: String? = null,
    val person: PersonProfileDto = PersonProfileDto(),
)

@Serializable
data class PersonProfileDto(
    val id: Int = 0,
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    val birthdate: String? = null,
    val isActive: Boolean = true,
)

@Serializable
data class ApiProblemDetailsDto(
    val title: String? = null,
    val status: Int? = null,
    val errors: Map<String, List<String>>? = null,
)

@Serializable
data class ApiErrorEnvelopeDto(
    val code: Int? = null,
    val message: String? = null,
    val details: String? = null,
)
