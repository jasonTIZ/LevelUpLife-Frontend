package com.example.leveluplife.data.player

import com.example.leveluplife.data.network.PlayerApi
import com.example.leveluplife.data.network.dto.GetPlayerProfileResponseDto
import com.example.leveluplife.data.network.dto.PersonUpdateRequestDto
import com.example.leveluplife.data.network.dto.PlayerDataUpdateRequestDto
import com.example.leveluplife.data.network.dto.PlayerProfileDto
import com.example.leveluplife.data.network.dto.UpdatePlayerProfileRequestDto
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

interface ProfileRepository {
    suspend fun fetchProfile(): Result<ProfileFetchResult>
    suspend fun updateProfile(
        etag: String,
        name: String,
        lastName: String,
        email: String,
        birthdate: String?,
        userName: String,
        bio: String,
    ): Result<PlayerProfile>
    suspend fun uploadAvatar(
        etag: String,
        sourceUri: String,
        mimeType: String?,
    ): Result<ProfileUpdateResult>
}

class DefaultProfileRepository(
    private val api: PlayerApi,
    private val profileCache: ProfileCache,
    private val avatarUploader: ProfileAvatarUploader,
    private val apiBaseUrl: String,
    private val json: Json,
) : ProfileRepository {

    override suspend fun fetchProfile(): Result<ProfileFetchResult> = try {
        val response = api.getProfile()
        val etag = response.headers()["ETag"].orEmpty()
        if (response.isSuccessful) {
            val body = response.body()
            if (body == null) {
                Result.failure(ProfileException(ProfileError.Unknown("Empty profile response.")))
            } else {
                val profile = body.toDomain(apiBaseUrl)
                profileCache.update(profile, etag)
                Result.success(ProfileFetchResult(profile = profile, etag = etag))
            }
        } else {
            Result.failure(
                ProfileException(
                    ProfileErrorMapper.fromHttpCode(response.code(), response.errorBody()?.string(), json),
                ),
            )
        }
    } catch (_: SocketTimeoutException) {
        Result.failure(ProfileException(ProfileError.Network("timeout")))
    } catch (_: UnknownHostException) {
        Result.failure(ProfileException(ProfileError.Network("unknown_host")))
    } catch (e: IOException) {
        Result.failure(ProfileException(ProfileError.Network(e.message ?: "network")))
    }

    override suspend fun updateProfile(
        etag: String,
        name: String,
        lastName: String,
        email: String,
        birthdate: String?,
        userName: String,
        bio: String,
    ): Result<PlayerProfile> = try {
        val request = UpdatePlayerProfileRequestDto(
            personData = PersonUpdateRequestDto(
                name = name.trim(),
                lastName = lastName.trim(),
                email = email.trim(),
                birthdate = birthdate?.trim()?.takeIf { it.isNotEmpty() },
            ),
            playerData = PlayerDataUpdateRequestDto(
                userName = userName.trim(),
                bio = bio.trim(),
            ),
        )
        val response = api.updateProfile(ifMatch = etag, body = request)
        val newEtag = response.headers()["ETag"].orEmpty()
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.player == null) {
                Result.failure(ProfileException(ProfileError.Unknown("Empty update response.")))
            } else {
                val profile = body.player.toDomain(apiBaseUrl)
                profileCache.update(profile, newEtag.ifBlank { null })
                Result.success(profile)
            }
        } else {
            Result.failure(
                ProfileException(
                    ProfileErrorMapper.fromHttpCode(response.code(), response.errorBody()?.string(), json),
                ),
            )
        }
    } catch (_: SocketTimeoutException) {
        Result.failure(ProfileException(ProfileError.Network("timeout")))
    } catch (_: UnknownHostException) {
        Result.failure(ProfileException(ProfileError.Network("unknown_host")))
    } catch (e: IOException) {
        Result.failure(ProfileException(ProfileError.Network(e.message ?: "network")))
    }

    override suspend fun uploadAvatar(
        etag: String,
        sourceUri: String,
        mimeType: String?,
    ): Result<ProfileUpdateResult> = try {
        val part = avatarUploader.createPart(sourceUri, mimeType)
            ?: return Result.failure(ProfileException(ProfileError.Unknown("Cannot read avatar file.")))

        val response = api.uploadAvatar(ifMatch = etag, file = part)
        val newEtag = response.headers()["ETag"].orEmpty()
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.player == null) {
                Result.failure(ProfileException(ProfileError.Unknown("Empty avatar upload response.")))
            } else {
                val profile = body.player.toDomain(apiBaseUrl)
                profileCache.update(profile, newEtag.ifBlank { null })
                Result.success(
                    ProfileUpdateResult(
                        profile = profile,
                        etag = newEtag.ifBlank { etag },
                    ),
                )
            }
        } else {
            Result.failure(
                ProfileException(
                    ProfileErrorMapper.fromHttpCode(response.code(), response.errorBody()?.string(), json),
                ),
            )
        }
    } catch (_: SocketTimeoutException) {
        Result.failure(ProfileException(ProfileError.Network("timeout")))
    } catch (_: UnknownHostException) {
        Result.failure(ProfileException(ProfileError.Network("unknown_host")))
    } catch (e: IOException) {
        Result.failure(ProfileException(ProfileError.Network(e.message ?: "network")))
    }
}

private fun GetPlayerProfileResponseDto.toDomain(apiBaseUrl: String): PlayerProfile = PlayerProfile(
    playerUserId = playerUserId,
    userName = playerUserUserName,
    level = playerUserLevel,
    name = personData.name,
    lastName = personData.lastName,
    email = personData.email,
    birthdate = personData.birthdate,
    avatarUri = ProfileImageUrls.resolve(avatarUrl, apiBaseUrl),
    bio = bio.orEmpty(),
)

private fun PlayerProfileDto.toDomain(apiBaseUrl: String): PlayerProfile = PlayerProfile(
    playerUserId = id.toString(),
    userName = userName,
    level = level,
    classId = classId,
    className = className,
    name = person.name,
    lastName = person.lastName,
    email = person.email,
    birthdate = person.birthdate,
    avatarUri = ProfileImageUrls.resolve(avatarUrl, apiBaseUrl),
    bio = bio.orEmpty(),
)
