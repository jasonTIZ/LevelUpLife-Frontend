package com.example.leveluplife.data.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RetryInterceptor(val maxAttempts: Int = 3) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var attempt = 0
        while (true) {
            attempt++
            try {
                val response = chain.proceed(request)
                // Don't retry: success, redirects, or client errors (4xx).
                // Only 5xx server errors are transient and worth retrying.
                if (!isRetryable(response.code) || attempt >= maxAttempts) return response
                response.close()
            } catch (e: IOException) {
                if (attempt >= maxAttempts) throw e
            }
        }
    }

    private fun isRetryable(code: Int) = code >= 500
}
