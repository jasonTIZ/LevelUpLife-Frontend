package com.example.leveluplife.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    @SerialName("personData")
    val personData: RegisterPersonData,
    @SerialName("playerUserData")
    val playerUserData: RegisterPlayerUserData,
)

@Serializable
data class RegisterPersonData(
    @SerialName("name")
    val name: String,
    @SerialName("lastName")
    val lastName: String,
    @SerialName("email")
    val email: String,
    @SerialName("birthdate")
    val birthdate: String,
)

@Serializable
data class RegisterPlayerUserData(
    @SerialName("userName")
    val userName: String,
    @SerialName("password")
    val password: String,
    @SerialName("classId")
    val classId: Int,
)
