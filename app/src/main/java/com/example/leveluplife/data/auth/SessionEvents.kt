package com.example.leveluplife.data.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SessionEvents {
    private val _events = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SessionEvent> = _events.asSharedFlow()

    private val emitted = mutableListOf<SessionEvent>()

    internal fun recordedEvents(): List<SessionEvent> = emitted.toList()

    internal fun clearRecordedEvents() = emitted.clear()

    fun notifyLoginSuccess() = emit(SessionEvent.SESSION_LOGIN_SUCCESS)

    fun notifyLogout() = emit(SessionEvent.SESSION_LOGOUT)

    fun notifyExpired() = emit(SessionEvent.SESSION_EXPIRED)

    fun notifyForbidden() = emit(SessionEvent.SESSION_FORBIDDEN)

    private fun emit(event: SessionEvent) {
        synchronized(emitted) { emitted.add(event) }
        _events.tryEmit(event)
    }
}
