package com.example.leveluplife.data.player

import android.content.Context
import android.net.Uri
import com.example.leveluplife.data.auth.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

interface ProfileAvatarStorage {
    suspend fun persistFromPickerUri(sourceUri: String, mimeType: String?): String?
    fun resolveDisplayUri(storedUri: String?): String?
}

class LocalProfileAvatarStorage(
    context: Context,
    private val tokenStore: TokenStore,
) : ProfileAvatarStorage {

    private val appContext = context.applicationContext

    override suspend fun persistFromPickerUri(sourceUri: String, mimeType: String?): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val extension = extensionFor(mimeType)
                val target = avatarFile(extension)
                deleteAvatarFilesForCurrentUser()

                appContext.contentResolver.openInputStream(Uri.parse(sourceUri))?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                } ?: throw IOException("Cannot open source avatar stream.")

                if (!target.exists() || target.length() <= 0L) {
                    throw IOException("Avatar file was not written.")
                }

                target.toURI().toString()
            }.getOrNull()
        }

    override fun resolveDisplayUri(storedUri: String?): String? {
        val value = storedUri?.trim().orEmpty()
        if (value.startsWith("http://", ignoreCase = true) ||
            value.startsWith("https://", ignoreCase = true) ||
            value.startsWith("content://", ignoreCase = true)
        ) {
            return value
        }
        migrateLegacyAvatarIfNeeded()
        value.toExistingFileOrNull()?.let { return it.toURI().toString() }
        return findExistingAvatarFileForCurrentUser()?.toURI()?.toString()
    }

    private fun migrateLegacyAvatarIfNeeded() {
        val key = currentUserKey()
        if (key == "anonymous") return
        SUPPORTED_EXTENSIONS.forEach { ext ->
            val legacy = File(appContext.filesDir, "profile_avatar.$ext")
            val scoped = avatarFile(ext)
            if (legacy.exists() && legacy.length() > 0L && !scoped.exists()) {
                legacy.renameTo(scoped)
            }
        }
    }

    private fun currentUserKey(): String =
        tokenStore.userId()?.takeIf { it.isNotBlank() } ?: "anonymous"

    private fun extensionFor(mimeType: String?): String = when (mimeType?.lowercase()) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        else -> "jpg"
    }

    private fun avatarFile(extension: String): File =
        File(appContext.filesDir, "profile_avatar_${currentUserKey()}.$extension")

    private fun findExistingAvatarFileForCurrentUser(): File? =
        SUPPORTED_EXTENSIONS
            .map(::avatarFile)
            .firstOrNull { it.exists() && it.length() > 0L }

    private fun deleteAvatarFilesForCurrentUser() {
        SUPPORTED_EXTENSIONS.forEach { ext ->
            avatarFile(ext).takeIf { it.exists() }?.delete()
        }
    }

    private fun String.toExistingFileOrNull(): File? = when {
        startsWith("file:") -> runCatching { File(java.net.URI(this)) }.getOrNull()
        startsWith("/") -> File(this)
        else -> null
    }?.takeIf { it.exists() && it.length() > 0L }

    private companion object {
        val SUPPORTED_EXTENSIONS = listOf("jpg", "png", "webp")
    }
}
