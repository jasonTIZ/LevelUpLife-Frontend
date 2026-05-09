package com.example.leveluplife.data.network.interceptor

import com.example.leveluplife.data.auth.JwtUtils
import com.example.leveluplife.data.auth.TokenStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenStore: TokenStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStore.accessToken()
        val request = if (token != null) {
            val userId = JwtUtils.extractSub(token)
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .apply { if (userId != null) header("X-User-Id", userId) }
                .build()
        } else {
            chain.request()
        }
        val response = chain.proceed(request)
        // 401 means the stored token is no longer valid — wipe it so the
        // app navigates back to login on the next isLoggedIn() check.
        if (response.code == 401) tokenStore.clear()
        return response
    }
}
