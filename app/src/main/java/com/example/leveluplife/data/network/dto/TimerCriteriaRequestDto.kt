package com.example.leveluplife.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Names aligned with the backend CreateTimerCriteriaRequestDto. */
@Serializable
data class TimerCriteriaRequestDto(
    @SerialName("NUM_SECONDS_DEFINED")
    val numSecondsDefined: Int,
    @SerialName("NUM_SECONDS_LONG")
    val numSecondsLong: Int? = null,
    @SerialName("TYPE_PAUSE_IS_ALLOWED")
    val typePauseIsAllowed: Boolean,
    @SerialName("STATUS_TIMER_CRITERIA_IS_ACTIVE")
    val statusTimerCriteriaIsActive: Boolean = true,
)
