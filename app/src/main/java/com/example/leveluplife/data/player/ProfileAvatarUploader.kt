package com.example.leveluplife.data.player

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileAvatarUploader(
    context: Context,
) {
    private val appContext = context.applicationContext

    fun createPart(sourceUri: String, mimeType: String?): MultipartBody.Part? {
        val uri = Uri.parse(sourceUri)
        val resolver = appContext.contentResolver
        val contentType = mimeType?.takeIf { it.isNotBlank() }
            ?: resolver.getType(uri)
            ?: "image/jpeg"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        if (bytes.isEmpty()) return null

        val extension = when (contentType.lowercase()) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val body = bytes.toRequestBody(contentType.toMediaType())
        return MultipartBody.Part.createFormData("file", "avatar.$extension", body)
    }
}
