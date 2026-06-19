package com.example.leveluplife.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
enum class TaskCompletionCriteria {
    REPETITIONS, TIMER, EVIDENCE
}