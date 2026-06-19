package com.example.leveluplife.data.player

import com.example.leveluplife.data.network.PlayerApi
import com.example.leveluplife.data.network.dto.GetPlayerProfileResponseDto
import com.example.leveluplife.data.network.dto.PersonUpdateRequestDto
import com.example.leveluplife.data.network.dto.PlayerDataUpdateRequestDto
import com.example.leveluplife.data.network.dto.PlayerProfileDto
import com.example.leveluplife.data.network.dto.UpdatePlayerProfileRequestDto
import com.example.leveluplife.data.network.dto.UpdatePlayerProfileResponseDto
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
    ): Result<PlayerProfile>
}

class DefaultProfileRepository(
    private val api: PlayerApi,
    private val profileCache: ProfileCache,
    private val avatarStorage: ProfileAvatarStorage,
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
                val cached = profileCache.profile.value
                val mergeLocalExtras = cached != null
                val avatarUri = when {
                    mergeLocalExtras && !cached?.avatarUri.isNullOrBlank() -> cached.avatarUri
                    mergeLocalExtras -> avatarStorage.resolveDisplayUri(null)
                    else -> null
                }
                val profile = body.toDomain(
                    avatarUri = avatarUri,
                    bio = if (mergeLocalExtras) cached?.bio.orEmpty() else "",
                )
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
            ),
        )
        val response = api.updateProfile(ifMatch = etag, body = request)
        val newEtag = response.headers()["ETag"].orEmpty()
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.player == null) {
                Result.failure(ProfileException(ProfileError.Unknown("Empty update response.")))
            } else {
                val cached = profileCache.profile.value
                val mergeLocalExtras = cached != null
                val avatarUri = when {
                    mergeLocalExtras && !cached?.avatarUri.isNullOrBlank() -> cached.avatarUri
                    mergeLocalExtras -> avatarStorage.resolveDisplayUri(null)
                    else -> null
                }
                val profile = body.player.toDomain(
                    avatarUri = avatarUri,
                    bio = if (mergeLocalExtras) cached?.bio.orEmpty() else "",
                )
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
}

private fun GetPlayerProfileResponseDto.toDomain(
    avatarUri: String?,
    bio: String,
): PlayerProfile = PlayerProfile(
    playerUserId = playerUserId,
    userName = playerUserUserName,
    level = playerUserLevel,
    name = personData.name,
    lastName = personData.lastName,
    email = personData.email,
    birthdate = personData.birthdate,
    avatarUri = avatarUri,
    bio = bio,
)

private fun PlayerProfileDto.toDomain(
    avatarUri: String?,
    bio: String,
): PlayerProfile = PlayerProfile(
    playerUserId = id.toString(),
    userName = userName,
    level = level,
    classId = classId,
    className = className,
    name = person.name,
    lastName = person.lastName,
    email = person.email,
    birthdate = person.birthdate,
    avatarUri = avatarUri,
    bio = bio,
)
