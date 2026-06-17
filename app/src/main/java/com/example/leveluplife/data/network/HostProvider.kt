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

        val finalHost = resolveHostForDevice(configuredHost)
        return configuredScheme to finalHost
    }

    /**
     * En emulador, `localhost` y las IPs LAN del PC (p. ej. 192.168.x.x) no alcanzan
     * el backend del host; hay que usar el alias `10.0.2.2` conservando el puerto.
     */
    private fun resolveHostForDevice(configuredHost: String): String {
        if (!isProbablyEmulator()) return configuredHost

        return when {
            configuredHost.startsWith("localhost") ->
                configuredHost.replaceFirst("localhost", EMULATOR_HOST_ALIAS)
            isPrivateLanHost(configuredHost) ->
                "$EMULATOR_HOST_ALIAS:${extractPort(configuredHost)}"
            else -> configuredHost
        }
    }

    private fun isPrivateLanHost(host: String): Boolean {
        val address = host.substringBefore(':')
        if (address.startsWith("192.168.") || address.startsWith("10.")) return true
        return PRIVATE_172_LAN.matches(address)
    }

    private fun extractPort(host: String): String =
        host.substringAfter(':', DEFAULT_PORT)

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

    companion object {
        private const val EMULATOR_HOST_ALIAS = "10.0.2.2"
        private const val DEFAULT_PORT = "5147"
        private val PRIVATE_172_LAN = Regex("""172\.(1[6-9]|2\d|3[01])\.\d+\.\d+""")
    }
}
