package com.example.leveluplife.data.habits

import android.content.Context
import android.net.Uri
import com.example.leveluplife.data.network.HabitTasksApi
import com.example.leveluplife.data.network.dto.CreateEvidenceRequest
import com.example.leveluplife.data.network.dto.EvidenceDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

interface EvidenceRepository {
    suspend fun getTaskEvidences(taskId: Int): Result<List<EvidenceDto>>
    suspend fun uploadEvidence(
        taskId: Int,
        fileUri: String,
        mimeType: String?,
        healthDataJson: String? = null,
    ): Result<EvidenceDto>
}

class DefaultEvidenceRepository(
    private val api: HabitTasksApi,
    private val context: Context,
) : EvidenceRepository {

    override suspend fun getTaskEvidences(taskId: Int): Result<List<EvidenceDto>> = try {
        val response = api.getTaskEvidences(taskId)
        when {
            response.isSuccessful -> Result.success(response.body() ?: emptyList())
            response.code() == 404 -> Result.failure(Exception("task_not_found"))
            else -> Result.failure(Exception("HTTP ${response.code()}"))
        }
    } catch (t: Throwable) {
        Result.failure(t)
    }

    override suspend fun uploadEvidence(
        taskId: Int,
        fileUri: String,
        mimeType: String?,
        healthDataJson: String?,
    ): Result<EvidenceDto> {
        return try {
            // Step 1: upload the file to get a hosted URL
            val fileUrl = uploadFile(fileUri, mimeType).getOrElse { return Result.failure(it) }

            // Step 2: create the evidence record with the URL
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val fecUploaded = sdf.format(Date())

            val request = CreateEvidenceRequest(
                url = fileUrl,
                healthDataJson = healthDataJson?.takeIf { it.isNotBlank() },
                uploadedAt = fecUploaded,
            )

            val response = api.createEvidence(taskId, request)
            when {
                response.isSuccessful -> {
                    val body = response.body()
                        ?: return Result.failure(Exception("empty_response"))
                    Result.success(body)
                }
                response.code() == 400 -> {
                    val errorBody = response.errorBody()?.string() ?: ""
                    Result.failure(Exception("upload_invalid_fields|$errorBody"))
                }
                response.code() == 404 -> Result.failure(Exception("task_not_found"))
                else -> {
                    val errorBody = response.errorBody()?.string() ?: ""
                    Result.failure(Exception("HTTP ${response.code()}|$errorBody"))
                }
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    private suspend fun uploadFile(fileUri: String, mimeType: String?): Result<String> {
        return try {
            val uri = Uri.parse(fileUri)
            val resolver = context.contentResolver
            val contentType = mimeType?.takeIf { it.isNotBlank() }
                ?: resolver.getType(uri)
                ?: "image/jpeg"
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return Result.failure(Exception("cannot_read_file"))
            if (bytes.isEmpty()) return Result.failure(Exception("cannot_read_file"))

            val extension = when {
                contentType.contains("png") -> "png"
                contentType.contains("webp") -> "webp"
                contentType.contains("mp4") -> "mp4"
                contentType.contains("pdf") -> "pdf"
                else -> "jpg"
            }

            val filePart = MultipartBody.Part.createFormData(
                "file",
                "evidence.$extension",
                bytes.toRequestBody(contentType.toMediaType()),
            )

            val response = api.uploadEvidenceFile(filePart)
            when {
                response.isSuccessful -> {
                    val url = response.body()?.url
                        ?: return Result.failure(Exception("empty_upload_response"))
                    Result.success(url)
                }
                else -> {
                    val errorBody = response.errorBody()?.string() ?: ""
                    Result.failure(Exception("upload_file_failed|HTTP ${response.code()}|$errorBody"))
                }
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
