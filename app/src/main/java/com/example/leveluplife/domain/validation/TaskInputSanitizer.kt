package com.example.leveluplife.domain.validation

object TaskInputSanitizer {

    fun digitsOnly(value: String, maxLength: Int = 6): String =
        value.filter { it.isDigit() }.take(maxLength)

    fun isoDateInput(value: String): String {
        val filtered = value.filter { it.isDigit() || it == '-' }
        return if (filtered.length <= 10) filtered else filtered.take(10)
    }

    fun trimToMax(value: String, maxLength: Int): String = value.take(maxLength)
}
