package com.example.leveluplife.data.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RetryInterceptor(
    val maxAttempts: Int = 3,
    private val backoffMs: (code: Int, retryAfterHeader: String?, attempt: Int) -> Long = computeDefaultBackoff,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var attempt = 0
        while (true) {
            attempt++
            try {
                val response = chain.proceed(request)
                // Don't retry: success, redirects, or 4xx client errors (except 429 rate-limit).
                // 5xx server errors and 429 are transient and worth retrying.
                if (!isRetryable(response.code) || attempt >= maxAttempts) return response
                val delay = backoffMs(response.code, response.header("Retry-After"), attempt)
                response.close()
                if (delay > 0L) Thread.sleep(delay)
            } catch (e: IOException) {
                if (attempt >= maxAttempts) throw e
            }
        }
    }

    private fun isRetryable(code: Int) = code >= 500 || code == 429

    companion object {
        private const val MAX_BACKOFF_MS = 30_000L

        val computeDefaultBackoff: (code: Int, retryAfterHeader: String?, attempt: Int) -> Long =
            { code, retryAfterHeader, attempt ->
                if (code == 429) {
                    retryAfterHeader?.toLongOrNull()?.times(1_000L)
                        ?: ((1L shl (attempt - 1)) * 1_000L).coerceAtMost(MAX_BACKOFF_MS)
                } else {
                    0L
                }
            }
    }
}
