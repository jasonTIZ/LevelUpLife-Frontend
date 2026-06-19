package com.example.leveluplife.data.auth

import com.example.leveluplife.data.error.AuthError
import com.example.leveluplife.data.error.AuthErrorMapper
import com.example.leveluplife.data.error.RegisterErrorMapper
import com.example.leveluplife.data.network.AuthApi
import com.example.leveluplife.data.network.dto.LoginRequest
import com.example.leveluplife.data.network.dto.RegisterPersonData
import com.example.leveluplife.data.network.dto.RegisterPlayerUserData
import com.example.leveluplife.data.network.dto.RegisterRequest
import com.example.leveluplife.data.player.ProfileCache
import kotlinx.serialization.json.Json

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthSession>
    suspend fun register(
        name: String,
        lastName: String,
        email: String,
        birthdate: String,
        userName: String,
        password: String,
        classId: Int,
    ): Result<RegisterOutcome>
    fun isLoggedIn(): Boolean
    suspend fun logout()
    fun clearLocalSession()
    fun currentTokens(): Pair<String?, String?>
}

class DefaultAuthRepository(
    private val api: AuthApi,
    private val tokenStore: TokenStore,
    private val sessionEvents: SessionEvents,
    private val profileCache: ProfileCache,
    private val json: Json,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthSession> = try {
        val response = api.login(LoginRequest(userNameOrEmail = email, password = password))
        if (response.isSuccessful) {
            val body = response.body()
                ?: return Result.failure(AuthErrorException(AuthError.Unknown("empty_body")))
            val data = body.data
                ?: return Result.failure(AuthErrorException(AuthError.Unknown("missing_data")))
            profileCache.clearMemory()
            val session = buildSession(data)
            tokenStore.saveTokens(session.accessToken, session.refreshToken, session.user.id)
            sessionEvents.notifyLoginSuccess()
            Result.success(session)
        } else {
            val raw = runCatching { response.errorBody()?.string() }.getOrNull()
            Result.failure(AuthErrorException(AuthErrorMapper.fromHttpCode(response.code(), raw, json)))
        }
    } catch (t: Throwable) {
        Result.failure(AuthErrorException(AuthErrorMapper.fromThrowable(t)))
    }

    override suspend fun register(
        name: String,
        lastName: String,
        email: String,
        birthdate: String,
        userName: String,
        password: String,
        classId: Int,
    ): Result<RegisterOutcome> = try {
        val response = api.register(
            RegisterRequest(
                personData = RegisterPersonData(
                    name = name.trim(),
                    lastName = lastName.trim(),
                    email = email.trim(),
                    birthdate = birthdate.trim(),
                ),
                playerUserData = RegisterPlayerUserData(
                    userName = userName.trim(),
                    password = password,
                    classId = classId,
                ),
            ),
        )
        if (response.isSuccessful) {
            val body = response.body()
                ?: return Result.failure(AuthErrorException(AuthError.Unknown("empty_body")))
            body.data?.let { data ->
                val session = buildSession(data)
                tokenStore.saveTokens(session.accessToken, session.refreshToken, session.user.id)
                return Result.success(RegisterOutcome.LoggedIn(session))
            }
            val successMessage = body.message?.takeIf { it.isNotBlank() }
                ?: "Usuario registrado exitosamente."
            login(userName.trim(), password).fold(
                onSuccess = { session -> Result.success(RegisterOutcome.LoggedIn(session)) },
                onFailure = { throwable ->
                    val authError = (throwable as? AuthErrorException)?.authError
                    if (authError is AuthError.InvalidCredentials) {
                        Result.success(RegisterOutcome.LoginRequired(successMessage))
                    } else {
                        Result.failure(throwable)
                    }
                },
            )
        } else {
            val raw = runCatching { response.errorBody()?.string() }.getOrNull()
            when (response.code()) {
                400 -> {
                    val fieldErrors = RegisterErrorMapper.fieldErrorsFrom400(raw, json)
                    if (fieldErrors.isNotEmpty()) {
                        Result.failure(RegisterValidationException(fieldErrors, AuthErrorMapper.parseMessage(raw, json)))
                    } else {
                        Result.failure(
                            AuthErrorException(AuthError.BadRequest(AuthErrorMapper.parseMessage(raw, json))),
                        )
                    }
                }
                409 -> {
                    val message = AuthErrorMapper.parseMessage(raw, json)
                    Result.failure(
                        AuthErrorException(
                            AuthError.DuplicateAccount(
                                message = message,
                                field = RegisterErrorMapper.duplicateAccountField(message),
                            ),
                        ),
                    )
                }
                else -> Result.failure(
                    AuthErrorException(AuthErrorMapper.fromHttpCode(response.code(), raw, json)),
                )
            }
        }
    } catch (t: Throwable) {
        when (t) {
            is RegisterValidationException -> Result.failure(t)
            is AuthErrorException -> Result.failure(t)
            else -> Result.failure(AuthErrorException(AuthErrorMapper.fromThrowable(t)))
        }
    }

    override fun isLoggedIn(): Boolean = tokenStore.hasSession()

    override suspend fun logout() {
        runCatching { api.logout() }
        clearLocalSession()
        profileCache.clearMemory()
        sessionEvents.notifyLogout()
    }

    override fun clearLocalSession() = tokenStore.clear()

    override fun currentTokens(): Pair<String?, String?> =
        tokenStore.accessToken() to tokenStore.refreshToken()

    private fun buildSession(data: com.example.leveluplife.data.network.dto.LoginData): AuthSession {
        val userId = JwtUtils.extractSub(data.token) ?: data.userName
        return AuthSession(
            accessToken = data.token,
            refreshToken = null,
            user = AuthUser.fromData(data, id = userId),
        )
    }
}

class AuthErrorException(val authError: AuthError) :
    RuntimeException(authError.message ?: authError::class.simpleName)
