package com.example.leveluplife.data.network.interceptor

import com.example.leveluplife.data.auth.TokenStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenStore: TokenStore) : Interceptor {

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
        if (response.code == 401) tokenStore.clear()
        return response
    }
}
