package com.example.leveluplife.data.network

import android.os.Build
import com.example.leveluplife.BuildConfig
import com.example.leveluplife.data.preferences.DebugApiPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Resuelve el host base del backend tomando en cuenta:
 *  1. Override runtime (DataStore debug) si se ha definido en builds debug.
 *  2. BuildConfig.API_HOST (default `localhost:5147` o el valor de local.properties).
 *  3. Si el host efectivo apunta a `localhost` y se ejecuta en emulador,
 *     se reemplaza automáticamente por `10.0.2.2` (IP del host del emulador).
 */
class HostProvider(
    private val debugPrefs: DebugApiPreferences,
) {
    fun resolveBaseUrl(): String {
        val (scheme, host) = resolveSchemeAndHost()
        return "$scheme://$host/"
    }

    fun resolveSchemeAndHost(): Pair<String, String> {
        val overrideHost = runCatching { runBlocking { debugPrefs.host.first() } }.getOrNull()
        val overrideScheme = runCatching { runBlocking { debugPrefs.scheme.first() } }.getOrNull()

        val configuredHost = overrideHost?.takeUnless { it.isBlank() } ?: BuildConfig.API_HOST
        val configuredScheme = overrideScheme?.takeUnless { it.isBlank() } ?: BuildConfig.API_SCHEME

        val finalHost = when {
            configuredHost.startsWith("localhost") && isProbablyEmulator() ->
                configuredHost.replaceFirst("localhost", "10.0.2.2")
            // Physical device + adb reverse: force IPv4 loopback (localhost may resolve to ::1).
            configuredHost.startsWith("localhost") ->
                configuredHost.replaceFirst("localhost", "127.0.0.1")
            else -> configuredHost
        }
        return configuredScheme to finalHost
    }

    private fun isProbablyEmulator(): Boolean {
        val fp = Build.FINGERPRINT?.lowercase().orEmpty()
        val model = Build.MODEL?.lowercase().orEmpty()
        val product = Build.PRODUCT?.lowercase().orEmpty()
        val hardware = Build.HARDWARE?.lowercase().orEmpty()
        return fp.contains("generic") ||
            fp.contains("emulator") ||
            model.contains("emulator") ||
            model.contains("sdk") ||
            product.contains("sdk") ||
            hardware.contains("goldfish") ||
            hardware.contains("ranchu")
    }
}
