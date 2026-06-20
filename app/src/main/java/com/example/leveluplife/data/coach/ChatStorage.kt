package com.example.leveluplife.data.coach

import android.content.Context
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private const val PREFS_NAME = "coach_chat_history"
private const val KEY_MESSAGES = "messages"
private const val KEY_LAST_TS = "last_ts"
private const val ONE_HOUR_MS = 60L * 60L * 1000L

class ChatStorage(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(ChatMessage.serializer())

    fun load(): List<ChatMessage> {
        val lastTs = prefs.getLong(KEY_LAST_TS, 0L)
        if (System.currentTimeMillis() - lastTs > ONE_HOUR_MS) {
            clear()
            return emptyList()
        }
        val raw = prefs.getString(KEY_MESSAGES, null) ?: return emptyList()
        return runCatching { json.decodeFromString(serializer, raw) }.getOrDefault(emptyList())
    }

    fun save(messages: List<ChatMessage>) {
        if (messages.isEmpty()) {
            clear()
            return
        }
        val raw = runCatching { json.encodeToString(serializer, messages) }.getOrNull() ?: return
        prefs.edit()
            .putString(KEY_MESSAGES, raw)
            .putLong(KEY_LAST_TS, System.currentTimeMillis())
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
