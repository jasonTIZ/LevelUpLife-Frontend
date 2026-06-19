package com.example.leveluplife.data.player

import com.example.leveluplife.data.auth.AuthRepository
import com.example.leveluplife.data.network.PlayerApi
import com.example.leveluplife.data.network.dto.DeletePlayerAccountRequest
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

data class DeactivateAccountResult(
    val message: String,
    val deactivatedAt: String,
)

sealed class DeactivateAccountError(open val message: String) {
    data class Unauthorized(override val message: String) : DeactivateAccountError(message)
    data class Forbidden(override val message: String) : DeactivateAccountError(message)
    data class NotFound(override val message: String) : DeactivateAccountError(message)
    data class Server(override val message: String) : DeactivateAccountError(message)
    data class Network(override val message: String) : DeactivateAccountError(message)
    data class Unknown(override val message: String) : DeactivateAccountError(message)
}

class DeactivateAccountException(val error: DeactivateAccountError) :
    Exception(error.message)

interface PlayerRepository {
    suspend fun deactivateAccount(reason: String? = null): Result<DeactivateAccountResult>
}

class DefaultPlayerRepository(
    private val api: PlayerApi,
    private val authRepository: AuthRepository,
) : PlayerRepository {

    override suspend fun deactivateAccount(reason: String?): Result<DeactivateAccountResult> = try {
        val trimmedReason = reason?.trim()?.takeIf { it.isNotEmpty() }
        val response = api.deactivateAccount(
            DeletePlayerAccountRequest(reason = trimmedReason),
        )
        when {
            response.isSuccessful -> {
                val body = response.body()
                val message = body?.message?.ifBlank { null }
                    ?: "Cuenta desactivada correctamente"
                authRepository.clearSessionAfterAccountDeactivation(message)
                Result.success(
                    DeactivateAccountResult(
                        message = message,
                        deactivatedAt = body?.deactivatedAt.orEmpty(),
                    ),
                )
            }
            response.code() == 401 -> Result.failure(
                DeactivateAccountException(
                    DeactivateAccountError.Unauthorized("Sesión expirada. Volvé a iniciar sesión."),
                ),
            )
            response.code() == 403 -> Result.failure(
                DeactivateAccountException(
                    DeactivateAccountError.Forbidden(
                        "No tenés permiso para desactivar esta cuenta o ya está inactiva.",
                    ),
                ),
            )
            response.code() == 404 -> Result.failure(
                DeactivateAccountException(
                    DeactivateAccountError.NotFound("No se encontró tu cuenta."),
                ),
            )
            response.code() in 500..599 -> Result.failure(
                DeactivateAccountException(
                    DeactivateAccountError.Server("Error del servidor. Intentá más tarde."),
                ),
            )
            else -> Result.failure(
                DeactivateAccountException(
                    DeactivateAccountError.Unknown("No se pudo desactivar la cuenta (HTTP ${response.code()})."),
                ),
            )
        }
    } catch (e: SocketTimeoutException) {
        Result.failure(DeactivateAccountException(DeactivateAccountError.Network("timeout")))
    } catch (e: UnknownHostException) {
        Result.failure(DeactivateAccountException(DeactivateAccountError.Network("unknown_host")))
    } catch (e: IOException) {
        Result.failure(DeactivateAccountException(DeactivateAccountError.Network(e.message ?: "network")))
    } catch (t: Throwable) {
        Result.failure(
            DeactivateAccountException(
                DeactivateAccountError.Unknown(t.message ?: "Error inesperado"),
            ),
        )
    }
}
