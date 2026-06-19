package com.example.leveluplife.data.player

object ProfileImageUrls {
    fun resolve(storedUrl: String?, apiBaseUrl: String): String? {
        val value = storedUrl?.trim().orEmpty()
        if (value.isEmpty()) return null
        if (
            value.startsWith("http://", ignoreCase = true) ||
            value.startsWith("https://", ignoreCase = true) ||
            value.startsWith("content://", ignoreCase = true) ||
            value.startsWith("file://", ignoreCase = true)
        ) {
            return value
        }
        val base = apiBaseUrl.trimEnd('/')
        val path = if (value.startsWith("/")) value else "/$value"
        return base + path
    }
}
