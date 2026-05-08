package com.example.leveluplife.data.auth

import com.example.leveluplife.data.error.AuthError
import com.example.leveluplife.data.error.AuthErrorMapper
import com.example.leveluplife.data.network.AuthApi
import com.example.leveluplife.data.network.dto.LoginRequest
import kotlinx.serialization.json.Json

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthSession>
    fun isLoggedIn(): Boolean
    fun logout()
    fun currentTokens(): Pair<String?, String?>
}

class DefaultAuthRepository(
    private val api: AuthApi,
    private val tokenStore: TokenStore,
    private val json: Json,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthSession> = try {
        val response = api.login(LoginRequest(userNameOrEmail = email, password = password))
        if (response.isSuccessful) {
            val body = response.body()
                ?: return Result.failure(AuthErrorException(AuthError.Unknown("empty_body")))
            val data = body.data
                ?: return Result.failure(AuthErrorException(AuthError.Unknown("missing_data")))
            // El backend solo devuelve el JWT; sacamos el id del claim `sub`
            // y caemos en el username como fallback si el token no se pudiera decodificar.
            val userId = JwtUtils.extractSub(data.token) ?: data.userName
            val session = AuthSession(
                accessToken = data.token,
                refreshToken = null,
                user = AuthUser.fromData(data, id = userId),
            )
            tokenStore.saveTokens(session.accessToken, session.refreshToken)
            Result.success(session)
        } else {
            val raw = runCatching { response.errorBody()?.string() }.getOrNull()
            Result.failure(AuthErrorException(AuthErrorMapper.fromHttpCode(response.code(), raw, json)))
        }
    } catch (t: Throwable) {
        Result.failure(AuthErrorException(AuthErrorMapper.fromThrowable(t)))
    }

    override fun isLoggedIn(): Boolean = tokenStore.hasSession()

    override fun logout() = tokenStore.clear()

    override fun currentTokens(): Pair<String?, String?> =
        tokenStore.accessToken() to tokenStore.refreshToken()
}

class AuthErrorException(val authError: AuthError) :
    RuntimeException(authError.message ?: authError::class.simpleName)
