package com.example.leveluplife.data.network.interceptor

import com.example.leveluplife.data.auth.SessionEvents
import com.example.leveluplife.data.auth.TokenStore
import com.example.leveluplife.data.network.ApiRoutes
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenStore: TokenStore,
    private val sessionEvents: SessionEvents,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStore.accessToken()
        val request = if (token != null) {
            val userId = tokenStore.userId()
            val builder = chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
            if (userId != null) {
                builder.header("X-User-Id", userId)
            }
            builder.build()
        } else {
            chain.request()
        }
        val response = chain.proceed(request)
        if (response.code == 401 && !ApiRoutes.isAuthRequestWithoutSession(request.url.encodedPath)) {
            tokenStore.clear()
            sessionEvents.notifyExpired()
        }
        return response
    }
}
