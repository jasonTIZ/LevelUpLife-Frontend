package com.example.leveluplife.ui.theme

import com.example.leveluplife.data.preferences.ThemePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeController(
    private val preferences: ThemePreferences,
    private val scope: CoroutineScope,
) {
    val mode: StateFlow<ThemeMode> = preferences.themeMode.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeMode.SYSTEM,
    )

    fun toggle(currentSystemIsDark: Boolean) {
        scope.launch {
            val current = preferences.themeMode.first()
            val effectiveDark = when (current) {
                ThemeMode.SYSTEM -> currentSystemIsDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            val next = if (effectiveDark) ThemeMode.LIGHT else ThemeMode.DARK
            preferences.setThemeMode(next)
        }
    }

    /** Fija LIGHT o DARK según el modo efectivo actual (p. ej. al iniciar sesión). */
    fun lockCurrentAppearance(currentSystemIsDark: Boolean) {
        scope.launch {
            val current = preferences.themeMode.first()
            if (current != ThemeMode.SYSTEM) return@launch
            val locked = if (currentSystemIsDark) ThemeMode.DARK else ThemeMode.LIGHT
            preferences.setThemeMode(locked)
        }
    }

    fun setMode(mode: ThemeMode) {
        scope.launch { preferences.setThemeMode(mode) }
    }
}
