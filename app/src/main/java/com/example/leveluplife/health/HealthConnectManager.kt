package com.example.leveluplife.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HealthConnectManager(private val context: Context) {

    enum class Metric { STEPS, DISTANCE, CALORIES, EXERCISE }

    data class Reading(val metric: Metric, val value: Double, val unit: String)

    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
    )

    fun isAvailable(): Boolean =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    suspend fun hasAllPermissions(): Boolean {
        if (!isAvailable()) return false
        val granted = HealthConnectClient.getOrCreate(context)
            .permissionController
            .getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    suspend fun readTodayTotal(metric: Metric): Reading {
        val client = HealthConnectClient.getOrCreate(context)
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now(zone).atStartOfDay(zone).toInstant()
        val range = TimeRangeFilter.between(start, Instant.now())

        return when (metric) {
            Metric.STEPS -> {
                val result = client.aggregate(AggregateRequest(setOf(StepsRecord.COUNT_TOTAL), range))
                Reading(metric, (result[StepsRecord.COUNT_TOTAL] ?: 0L).toDouble(), "steps")
            }
            Metric.DISTANCE -> {
                val result = client.aggregate(AggregateRequest(setOf(DistanceRecord.DISTANCE_TOTAL), range))
                Reading(metric, result[DistanceRecord.DISTANCE_TOTAL]?.inKilometers ?: 0.0, "km")
            }
            Metric.CALORIES -> {
                val result = client.aggregate(
                    AggregateRequest(setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL), range),
                )
                Reading(metric, result[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0, "kcal")
            }
            Metric.EXERCISE -> {
                val result = client.aggregate(
                    AggregateRequest(setOf(ExerciseSessionRecord.EXERCISE_DURATION_TOTAL), range),
                )
                Reading(metric, (result[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL]?.toMinutes() ?: 0L).toDouble(), "min")
            }
        }
    }

    fun toHealthDataJson(reading: Reading): String {
        val date = LocalDate.now().toString()
        return """{"metric":"${reading.metric.name}","value":${reading.value},"unit":"${reading.unit}","date":"$date"}"""
    }
}
