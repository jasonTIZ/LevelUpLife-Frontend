package com.example.leveluplife.data.habits

import com.example.leveluplife.domain.validation.HabitTaskFieldError
import com.example.leveluplife.domain.validation.HabitTaskFormErrors
import com.example.leveluplife.domain.validation.HabitTaskValidators
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

class HabitTaskValidationFailure(
    val fieldErrors: HabitTaskFormErrors,
    override val message: String,
) : Exception(message)

object HabitTaskApiErrorParser {

    private val json = Json { ignoreUnknownKeys = true }

    fun parse400(errorBody: String?): ParsedApiError {
        if (errorBody.isNullOrBlank()) {
            return ParsedApiError(
                summary = "Datos inválidos. Revisa los campos marcados.",
                fieldErrors = HabitTaskFormErrors(),
            )
        }

        val root = runCatching {
            json.parseToJsonElement(errorBody).jsonObject
        }.getOrNull() ?: return ParsedApiError(
            summary = "Datos inválidos. Revisa los campos marcados.",
            fieldErrors = HabitTaskFormErrors(),
        )

        val errorsNode = root["errors"]?.jsonObject
        if (errorsNode == null) {
            val detail = root["details"]?.asStringOrNull()
                ?: root["title"]?.asStringOrNull()
            return ParsedApiError(
                summary = detail ?: "Datos inválidos. Revisa los campos marcados.",
                fieldErrors = HabitTaskFormErrors(),
            )
        }

        val fieldErrors = mapServerErrors(errorsNode)
        val firstMessage = firstErrorMessage(errorsNode)
        val summary = when {
            fieldErrors.hasErrors -> "Revisa los campos marcados."
            firstMessage != null -> firstMessage
            else -> root["title"]?.asStringOrNull() ?: "Datos inválidos."
        }
        return ParsedApiError(summary = summary, fieldErrors = fieldErrors)
    }

    private fun mapServerErrors(errors: JsonObject): HabitTaskFormErrors {
        fun messagesFor(vararg keys: String): List<String> =
            keys.flatMap { key ->
                errors[key]?.toMessageList() ?: emptyList()
            }

        fun mapField(vararg keys: String): HabitTaskFieldError? {
            val messages = messagesFor(*keys)
            if (messages.isEmpty()) return null
            val text = messages.joinToString(" ").lowercase()
            return when {
                "required" in text -> HabitTaskFieldError.Required
                "between" in text || "length" in text || "characters" in text ->
                    HabitTaskFieldError.TooShort(HabitTaskValidators.TITLE_MIN)
                "greater than" in text || "range" in text -> HabitTaskFieldError.InvalidNumber
                else -> HabitTaskFieldError.InvalidNumber
            }
        }

        return HabitTaskFormErrors(
            habitId = mapField("habitId", "HabitId"),
            title = mapField("title", "Title"),
            difficulty = mapField("difficulty", "Difficulty"),
            frequency = mapField("frequency", "Frequency"),
            periodLength = mapField("periodLength", "PeriodLength"),
            periodUnit = mapField("periodUnit", "PeriodUnit"),
            startDate = mapField("startDate", "StartDate"),
            completionCriteria = mapField("completionCriteria", "CompletionCriteria"),
            repetitions = mapField(
                "repetitionCriteria.repetitions",
                "RepetitionCriteria.Repetitions",
                "repetitions",
            ),
            measurementUnit = mapField(
                "repetitionCriteria.measurementUnit",
                "RepetitionCriteria.MeasurementUnit",
                "measurementUnit",
            ),
            evidence = mapField("evidence", "Evidence"),
            timerSeconds = mapField(
                "timerCriteria",
                "TimerCriteria",
                "timerCriteria.numSecondsDefined",
                "TimerCriteria.NUM_SECONDS_DEFINED",
                "TimerCriteria.NumSecondsDefined",
            ),
        )
    }

    private fun firstErrorMessage(errors: JsonObject): String? =
        errors.values
            .flatMap { it.toMessageList() }
            .firstOrNull { it.isNotBlank() }

    private fun JsonElement.toMessageList(): List<String> = when (this) {
        is JsonArray -> mapNotNull { (it as? JsonPrimitive)?.asStringOrNull() }
        is JsonPrimitive -> listOfNotNull(asStringOrNull())
        else -> emptyList()
    }

    private fun JsonElement.asStringOrNull(): String? =
        (this as? JsonPrimitive)?.takeIf { it.isString }?.content
}

data class ParsedApiError(
    val summary: String,
    val fieldErrors: HabitTaskFormErrors,
)
